package com.panda.mall.canal.client;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.panda.mall.canal.config.CanalRabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.alibaba.otter.canal.protocol.Message;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CanalClient implements CommandLineRunner {

    @Value("${canal.server}")
    private String canalServer;
    @Value("${canal.destination}")
    private String destination;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public void run(String... args) {
        String[] parts = canalServer.split(":");
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(parts[0], Integer.parseInt(parts[1])), destination, "", "");

        new Thread(() -> {
            try {
                connector.connect();
                connector.subscribe(".*\\..*");
                connector.rollback();

                while (true) {
                    Message message = connector.getWithoutAck(100);
                    long batchId = message.getId();
                    int size = message.getEntries().size();

                    if (batchId == -1 || size == 0) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        handleEntries(message.getEntries());
                    }

                    connector.ack(batchId);
                }
            } finally {
                connector.disconnect();
            }
        }).start();
    }

    private void handleEntries(List<CanalEntry.Entry> entries) {
        for (CanalEntry.Entry entry : entries) {
            // 只处理行数据变更
            if (entry.getEntryType() == CanalEntry.EntryType.ROWDATA) {
                try {
                    CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                    String tableName = entry.getHeader().getTableName();
                    CanalEntry.EventType eventType = rowChange.getEventType();

                    if ("pms_product".equalsIgnoreCase(tableName)
                            && (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE)) {
                        for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                            sendProductSyncMessage(rowData.getAfterColumnsList());
                        }
                    }
                } catch (Exception e) {
                    log.warn("Canal处理数据失败");
                }
            }
        }
    }

    private void sendProductSyncMessage(List<CanalEntry.Column> columns) {
        Map<String, String> columnMap = new HashMap<>();
        for (CanalEntry.Column column : columns) {
            columnMap.put(column.getName(), column.getValue());
        }

        String productId = columnMap.get("id");
        if (productId != null) {
            rabbitTemplate.convertAndSend(CanalRabbitConfig.PRODUCT_SYNC_EXCHANGE, CanalRabbitConfig.PRODUCT_SYNC_ROUTING_KEY, productId);
            log.info("检测到商品变更, ID:{}，已发送同步消息至MQ。", productId);
        }

    }
}
