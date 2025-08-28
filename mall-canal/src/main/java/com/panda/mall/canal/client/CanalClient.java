package com.panda.mall.canal.client;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.panda.mall.common.domain.QueueEnum;
import com.panda.mall.common.dto.CacheSyncMessage;
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
import java.util.Objects;

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
                CanalEntry.RowChange rowChange;
                try {
                    rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                    String tableName = entry.getHeader().getTableName();
                    CanalEntry.EventType eventType = rowChange.getEventType();
                    // 同步ES
                    if ("pms_product".equalsIgnoreCase(tableName)
                            && (eventType == CanalEntry.EventType.INSERT || eventType == CanalEntry.EventType.UPDATE)) {
                        for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
                            sendProductSyncMessage(rowData.getAfterColumnsList());
                        }
                    }

                    // 同步缓存
                    if (eventType == CanalEntry.EventType.UPDATE || eventType == CanalEntry.EventType.DELETE) {
                        sendCacheSyncMessage(tableName, rowChange);
                    }

                } catch (Exception e) {
                    log.warn("Canal处理数据失败", e);
                }
            }
        }
    }

    private void sendCacheSyncMessage(String tableName, CanalEntry.RowChange rowChange) {
        String keyId = null;
        CacheSyncMessage message = new CacheSyncMessage();
        List<CanalEntry.RowData> rowDataList = rowChange.getRowDatasList();
        for (CanalEntry.RowData rowData : rowDataList) {
            // DELETE需要获取旧数据，UPDATE都可以
            List<CanalEntry.Column> columns = (rowChange.getEventType() == CanalEntry.EventType.DELETE)
                    ? rowData.getBeforeColumnsList()
                    : rowData.getAfterColumnsList();
            if ("pms_product".equalsIgnoreCase(tableName)) {
                keyId = getColumnValue(columns, "id");
                message.setType("PRODUCT"); // 设置消息类型
                message.setKeyId(keyId);
            } else if ("pms_sku_stock".equalsIgnoreCase(tableName)) {
                // SKU的缓存通常与整个商品相关联，所以我们发送product_id来清理整个商品的缓存
                keyId = getColumnValue(columns, "product_id");
                message.setType("PRODUCT"); // SKU变更也属于商品变更
                message.setKeyId(keyId);
            } else if ("pms_product_category".equalsIgnoreCase(tableName)) {
                keyId = getColumnValue(columns, "id");
                message.setType("PRODUCT_CATEGORY");
                message.setKeyId(keyId);
                // 示例：如果分类的父ID变了，可以把旧的父ID放到payload里
                // String oldParentId = getColumnValue(rowData.getBeforeColumnsList(), "parent_id");
                // message.setPayload(Map.of("oldParentId", oldParentId));
            }
            // ... 在这里可以继续添加其他需要进行缓存同步的表

            if (keyId != null) {
                log.info("发送缓存清理消息:{}", message);
                // 发送消息到缓存同步专用的交换机和路由键
                rabbitTemplate.convertAndSend(QueueEnum.CACHE_SYNC.getExchange(), QueueEnum.CACHE_SYNC.getRouteKey(), message);
            }

        }
    }

    /**
     * 从列数据列表中获取指定列名的值
     */
    private String getColumnValue(List<CanalEntry.Column> columns, String columnName) {
        for (CanalEntry.Column column : columns) {
            if (Objects.equals(column.getName(), columnName)) {
                return column.getValue();
            }
        }
        return null;
    }

    private void sendProductSyncMessage(List<CanalEntry.Column> columns) {
        Map<String, String> columnMap = new HashMap<>();
        for (CanalEntry.Column column : columns) {
            columnMap.put(column.getName(), column.getValue());
        }

        String productId = columnMap.get("id");
        if (productId != null) {
            rabbitTemplate.convertAndSend(QueueEnum.PRODUCT_SYNC.getExchange(), QueueEnum.PRODUCT_SYNC.getRouteKey(), productId);
            log.info("检测到商品变更, ID:{}，已发送同步消息至MQ。", productId);
        }

    }
}
