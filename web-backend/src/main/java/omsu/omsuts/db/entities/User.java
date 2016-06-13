package omsu.omsuts.db.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * Created by sds on 6/9/16.
 */

@DatabaseTable(tableName = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @DatabaseField(id = true)
    private String name;
    @DatabaseField
    private String password;
    @DatabaseField
    private int score;
}
