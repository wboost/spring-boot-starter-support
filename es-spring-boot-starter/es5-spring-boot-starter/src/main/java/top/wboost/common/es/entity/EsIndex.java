package top.wboost.common.es.entity;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.util.*;
import java.util.Map.Entry;

/**
 * ES索引实体类
 * @author jwSun
 * @date 2017年4月17日 下午4:53:25
 */
public class EsIndex extends BaseEsIndex {
    private Integer number_of_shards; //分片数  
    private Integer number_of_replicas; //备份数
    private List<String> alias = null;// 别名
    private Map<String, Set<Field>> fields = new HashMap<>();//字段

    public void addAlias(String alias) {
        if (this.alias == null) {
            this.alias = new ArrayList<>();
        }
        this.alias.add(alias);
    }

    private BuilderSupport builderSupport = null;
    private SettingSupport settingSupport = null;

    public SettingSupport getSettingSupport() {
        return settingSupport;
    }

    public EsIndex setSettingSupport(SettingSupport settingSupport) {
        this.settingSupport = settingSupport;
        return this;
    }

    public EsIndex setBuilderSupport(BuilderSupport builderSupport) {
        this.builderSupport = builderSupport;
        return this;
    }

    public BuilderSupport getBuilderSupport() {
        return builderSupport;
    }

    public interface BuilderSupport {
        public void invoke(CreateIndexRequestBuilder createIndexRequestBuilder);
    }

    public interface SettingSupport {
        public void invoke(Settings settings);
    }

    public EsIndex(String index, String type, Integer number_of_shards, Integer number_of_replicas) {
        super(index, type);
        this.number_of_shards = number_of_shards;
        this.number_of_replicas = number_of_replicas;
    }

    public EsIndex putField(String fieldName, String fieldKey, String fieldValue) {
        Field field = new Field(fieldKey, fieldValue);
        if (fields.containsKey(fieldName)) {
            fields.get(fieldName).add(field);
        } else {
            Set<Field> fieldSet = new HashSet<Field>();
            fieldSet.add(field);
            fields.put(fieldName, fieldSet);
        }
        return this;
    }

    public Map<String, Set<Field>> getFields() {
        return fields;
    }

    public void setFields(Map<String, Set<Field>> fields) {
        this.fields = fields;
    }

    public XContentBuilder getProperties() {
        XContentBuilder mapping = null;
        try {
            mapping = XContentFactory.jsonBuilder().startObject().startObject("properties");
            for (Entry<String, Set<Field>> entry : getFields().entrySet()) {
                mapping.startObject(entry.getKey());
                for (Field field : entry.getValue()) {
                    mapping.field(field.getName(), field.getValue());
                }
                mapping.endObject();
            }
            mapping.endObject().endObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapping;
    }

    public Integer getNumber_of_shards() {
        return number_of_shards;
    }

    public void setNumber_of_shards(Integer number_of_shards) {
        this.number_of_shards = number_of_shards;
    }

    public Integer getNumber_of_replicas() {
        return number_of_replicas;
    }

    public void setNumber_of_replicas(Integer number_of_replicas) {
        this.number_of_replicas = number_of_replicas;
    }
}
