# json库扩展

::: tip
- 目前支持Snack3、Jackson、fastjson和Gson四种json库

:::

## json库扩展
- 扩展json库需要实现`JsonConvert`接口，并实现`strToMap`和`mapToStr`方法
- 并通过spi机制加载，可参照`warm-flow-plugin-json`模块
- 在resource目录下新建`META-INF\services`文件夹，并在该文件夹下新建文件`org.dromara.warm.flow.core.json.JsonConvert`, 配置实现类的全限定名，如`org.dromara.warm.plugin.json.JsonConvertJackson`

```java
public class JsonConvertJackson implements JsonConvert {

    private static final Logger log = LoggerFactory.getLogger(JsonConvertJackson.class);

    /**
     * 将字符串转为map
     * @param jsonStr json字符串
     * @return map
     */
    @Override
    public Map<String, Object> strToMap(String jsonStr) {
        if (StringUtils.isNotEmpty(jsonStr)) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.readValue(jsonStr, TypeFactory.defaultInstance().constructMapType(Map.class, String.class, Object.class));
            } catch (IOException e) {
                log.error("json转换异常", e);
                throw new FlowException("json转换异常");
            }
        }
        return new HashMap<>();
    }

    /**
     * 将map转为字符串
     * @param variable map
     * @return json字符串
     */
    @Override
    public String mapToStr(Map<String, Object> variable) {
        if (MapUtil.isNotEmpty(variable)) {
            // 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return objectMapper.writeValueAsString(variable);
            } catch (Exception e) {
                log.error("Map转换异常", e);
                throw new FlowException("Map转换异常");
            }
        }
        return null;
    }

}
```
