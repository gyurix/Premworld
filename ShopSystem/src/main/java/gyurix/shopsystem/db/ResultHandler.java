package gyurix.shopsystem.db;

import java.sql.ResultSet;

public interface ResultHandler {
  void handle(ResultSet rs) throws Throwable;
}
