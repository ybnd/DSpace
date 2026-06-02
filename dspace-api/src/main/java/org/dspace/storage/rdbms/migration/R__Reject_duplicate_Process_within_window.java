/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.dspace.storage.rdbms.migration;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.dspace.services.ConfigurationService;
import org.dspace.services.factory.DSpaceServicesFactory;
import org.dspace.storage.rdbms.DatabaseUtils;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/**
 * Adds an exclusion constraint to prevent duplicate Processes from getting added within a given time window.
 * <br>
 * Supplements {@code ScriptRestRepository#checkNewProcessForExistingDuplicates} for the case where the same Process was
 * started twice in quick succession, such that the first request did not finish before the second request was received.
 * <br>
 * The time window is configured as {@code process.reject.duplicate.within.ms} in {@code dspace.cfg}.
 * <br>
 * Run {@code ./dspace database migrate} to apply this configuration change to the database.
 */
public class R__Reject_duplicate_Process_within_window extends BaseJavaMigration {
    protected Integer checksum;

    @Override
    public void migrate(Context context) throws Exception {
        String dataMigrateSQL = getSQL(DatabaseUtils.getDbType(context.getConnection()));

        // org/dspace/storage/rdbms/sqlmigration/processes/postgres/V7.6_2026.06.02__Reject_duplicate_Process_within_window.sql

        // Replace ${handle.canonical.prefix} variable in SQL script with value from Configuration
        Map<String, String> valuesMap = new HashMap<String, String>();

        valuesMap.put("half_window", String.valueOf(getWindow() / 2));
        valuesMap.put("after", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        dataMigrateSQL = sub.replace(dataMigrateSQL);

        DatabaseUtils.executeSql(context.getConnection(), dataMigrateSQL);
    }

    @Override
    public Integer getChecksum() {
        if (checksum == null) {
            try {
                checksum = getSQL("postgres").hashCode() + 23 * getWindow();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return checksum;
    }

    private String getSQL(String dbtype) throws SQLException {
        String sqlMigrationPath = "org/dspace/storage/rdbms/sqlmigration/processes/" + dbtype + "/";
        return MigrationUtils.getResourceAsString(
            sqlMigrationPath + "R__Reject_duplicate_Process_within_window.sql"
        );
    }

    private int getWindow() {
        ConfigurationService configurationService = DSpaceServicesFactory.getInstance().getConfigurationService();
        return configurationService.getIntProperty("process.reject.duplicate.within.ms", 1000);
    }
}
