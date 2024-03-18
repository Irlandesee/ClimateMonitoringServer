package it.uninsubria.update;

import it.uninsubria.servercm.ServerInterface.Tables;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public record Update(Tables table, String updateId, LocalDateTime lastModified) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public String toString() {
        return "Update{"+this.table + ":" + this.updateId + ":" + this.lastModified+"}";
    }

}
