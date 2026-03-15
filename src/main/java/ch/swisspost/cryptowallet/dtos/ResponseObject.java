package ch.swisspost.cryptowallet.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseObject<T> {

    private ResponseStatus status;

    private String message;

    private T data;


    public enum ResponseStatus {

        SUCCESSFUL("Successful"),

        FAILED("Failed");

        private final String value;


        ResponseStatus(String value) {
            this.value = value;
        }


        @JsonValue
        public String getValue() {
            return value;
        }
    }
}


