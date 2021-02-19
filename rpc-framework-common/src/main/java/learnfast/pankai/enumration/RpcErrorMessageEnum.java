package learnfast.pankai.enumration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public enum RpcErrorMessageEnum {
    SERVICE_INVOCATION_FAILURE("服务调用失败"),
    SERVICE_CAN_NOT_BE_FOUND("未能找到指定的服务"),
    SERVICE_NOT_IMPLEMENT_ANY_INTERFACES("待调用的服务未实现任何接口");
    private final String message;

}
