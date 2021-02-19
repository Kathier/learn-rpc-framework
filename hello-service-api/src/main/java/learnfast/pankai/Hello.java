package learnfast.pankai;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author PanKai
 * @Description 测试用api的实体
 * @Date  2021/2/18/ 16:44
 **/
@Data
public class Hello  implements Serializable {
    private  String message;
    private String description;
    public Hello(String message, String description) {
        this.message = message;
        this.description = description;
    }
}
