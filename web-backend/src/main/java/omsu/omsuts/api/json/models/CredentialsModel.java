package omsu.omsuts.api.json.models;

import lombok.Data;

/**
 * Created by sds on 6/9/16.
 */

@Data
public class CredentialsModel {
    private String login;
    private String password;

    public boolean isValid() {
        return login != null && !login.isEmpty() && password != null && !password.isEmpty();
    }
}
