package io.etrace.api.thirdparty.dingtalk;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class TextMessage {
    String msgtype = "text";
    Text text;
    At at;

    public TextMessage(String text, At at) {
        this.text = new Text(text);
        this.at = at;
    }

    @Data
    @AllArgsConstructor
    private static class Text {
        String content;
    }
}
