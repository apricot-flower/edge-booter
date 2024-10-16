package on.edge.server.web;

import java.io.Serializable;

/**
 * 统一返回类
 * @param <T>
 */
public class BaseUnifiedResponse<T> implements Serializable {

    private Boolean success;

    private Integer code;

    private String message;

    private Integer total;

    private T data;

    public BaseUnifiedResponse() {
    }

    public BaseUnifiedResponse(Boolean success, Integer code, String message, Integer total, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.total = total;
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static BaseUnifiedResponse<?> error() {
        BaseUnifiedResponse<?> result = new BaseUnifiedResponse<>();
        result.setSuccess(false);
        result.setCode(500);
        result.setMessage("请求失败");
        return result;
    }

    public static BaseUnifiedResponse<?> error(String message) {
        BaseUnifiedResponse<?> result = new BaseUnifiedResponse<>();
        result.setSuccess(false);
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static BaseUnifiedResponse<?> success() {
        BaseUnifiedResponse<?> result = new BaseUnifiedResponse<>();
        result.setSuccess(true);
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    public static BaseUnifiedResponse<?> success(String message) {
        BaseUnifiedResponse<?> result = new BaseUnifiedResponse<>();
        result.setSuccess(true);
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    public static <T> BaseUnifiedResponse<T> success(T data) {
        BaseUnifiedResponse<T> result = new BaseUnifiedResponse<>();
        result.setSuccess(true);
        result.setData(data);
        result.setCode(200);
        result.setMessage("success");
        return result;
    }

    public static <T> BaseUnifiedResponse<T> success(T data, String message) {
        BaseUnifiedResponse<T> result = new BaseUnifiedResponse<>();
        result.setSuccess(true);
        result.setData(data);
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    public static <T> BaseUnifiedResponse<T> success(T data, Integer total) {
        BaseUnifiedResponse<T> result = new BaseUnifiedResponse<>();
        result.setSuccess(true);
        result.setData(data);
        result.setCode(200);
        result.setMessage("success");
        result.setTotal(total);
        return result;
    }

    public static <T> BaseUnifiedResponse<T> common(Boolean success, Integer code, String message, Integer total, T data) {
        BaseUnifiedResponse<T> result = new BaseUnifiedResponse<>();
        result.setSuccess(success);
        result.setData(data);
        result.setCode(code);
        result.setMessage(message);
        result.setTotal(total);
        return result;
    }
}
