package pl.kuezese.auth.bungee.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import pl.kuezese.auth.bungee.type.ResultType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Getter @RequiredArgsConstructor
public class ResultData {

    private final String name;
    private final ResultType result;
    private final Timestamp expireDate;

    public ResultData(ResultSet rs) throws SQLException {
        name = rs.getString("name");
        result = ResultType.findByName(rs.getString("result"));
        expireDate = rs.getTimestamp("expire_date");
    }
}
