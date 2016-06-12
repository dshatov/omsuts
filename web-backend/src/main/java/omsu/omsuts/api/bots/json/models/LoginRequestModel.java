package omsu.omsuts.api.bots.json.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by sds on 6/12/16.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestModel {
    private String username;
    private String password;

    public boolean isValid() {
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }
}