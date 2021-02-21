package learnfast.pankai.dto;

import learnfast.pankai.enumration.RpcResponseCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by PanKai on 2021/2/18 21:59
 *
 * @Description
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
public class RpcResponse <T> implements Serializable {
    private  static  final long serialVersionUID=715745410605631233L;

    //响应码
    private  Integer code;

    //响应消息

    private  String message;

    //响应数据

    private  T data;

    public  static <T> RpcResponse<T> success(T data){
        RpcResponse<T> response=new RpcResponse<>();
        response.setCode(RpcResponseCode.SUCCESS.getCode());
        if(data!=null){
            response.setData(data);
        }

        return  response;
    }

    public  static <T> RpcResponse<T> fail(RpcResponseCode rpcResponseCode){
        RpcResponse<T> response=new RpcResponse<>();
        response.setCode(rpcResponseCode.getCode());
        response.setMessage(rpcResponseCode.getMessage());
        return  response;
    }


}
