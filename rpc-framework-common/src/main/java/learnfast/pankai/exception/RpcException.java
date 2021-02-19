package learnfast.pankai.exception;

import learnfast.pankai.dto.RpcRequest;
import learnfast.pankai.enumration.RpcErrorMessageEnum;

/**
 * Created by PanKai on 2021/2/18 21:38
 *
 * @Description
 **/
public class RpcException extends RuntimeException{
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum,String detail){
        super(rpcErrorMessageEnum.getMessage()+":"+detail);
    }
    public  RpcException(String message,Throwable cause){
        super(message,cause);
    }
    public  RpcException(RpcErrorMessageEnum rpcErrorMessageEnum){
        super(rpcErrorMessageEnum.getMessage());

    }
}
