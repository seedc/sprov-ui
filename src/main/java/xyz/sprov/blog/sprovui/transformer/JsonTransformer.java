package xyz.sprov.blog.sprovui.transformer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import spark.ResponseTransformer;

public class JsonTransformer implements ResponseTransformer {

    @Override
    public String render(Object model) throws Exception {
        return JSON.toJSONString(model, SerializerFeature.WriteMapNullValue);
    }

}
