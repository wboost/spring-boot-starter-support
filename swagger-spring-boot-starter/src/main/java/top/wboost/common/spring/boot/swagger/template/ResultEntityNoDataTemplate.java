package top.wboost.common.spring.boot.swagger.template;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel
public class ResultEntityNoDataTemplate {

    @ApiModelProperty(value = "返回系统提示参数(返回码,提示信息)")
    private ReturnInfoTemplate info = new ReturnInfoTemplate();
    @ApiModelProperty(value = "验证|true或无-登录状态|false-未登录", allowEmptyValue = true)
    private Boolean validate;
    @ApiModelProperty("状态|0-成功|1-失败")
    private int status;

    @Data
    @ApiModel
    class ReturnInfoTemplate {
        @ApiModelProperty(value = "返回码")
        private Integer code;
        @ApiModelProperty("提示信息")
        private String message;
    }

}
