package com.gmail.nossr50.runnables.backups;

import com.gmail.nossr50.mcMMO;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CleanBackupFilesTask extends BukkitRunnable {
    private final mcMMO pluginRef;
    private final File BACKUP_DIR;

    public CleanBackupFilesTask(mcMMO pluginRef) {
        this.pluginRef = pluginRef;

        BACKUP_DIR = new File(pluginRef.getMainDirectory() + "backup" + File.separator);
    }

    @Override
    public void run() {
        List<Integer> savedDays = new ArrayList<>(); //TODO: Should probably look into why this isn't used...
        HashMap<Integer, List<Integer>> savedYearsWeeks = new HashMap<>(); //TODO: Should probably look into why this isn't used...
        List<File> toDelete = new ArrayList<>();
        int amountTotal = 0;
        int amountDeleted = 0;
        int oldFileAgeLimit = pluginRef.getConfigManager().getConfigAutomatedBackups().getBackupDayLimit();

        //Don't remove files unless there is at least 10 of them
        if (BACKUP_DIR.listFiles() == null || BACKUP_DIR.listFiles().length < 10)
            return;

        // Check files in backup folder from oldest to newest
        for (File file : BACKUP_DIR.listFiles()) {
            if (!file.isFile() || file.isDirectory()) {
                continue;
            }

            amountTotal++;
            String fileName = file.getName();

            Date date = getDate(fileName.split("[.]")[0]);

            if (!fileName.contains(".zip") || date == null) {
                pluginRef.debug("Could not determine date for file: " + fileName);
                continue;
            }

            long fileSaveTimeStamp = date.getTime();
            long currentTime = System.currentTimeMillis();

            //File is not old enough so don't delete it
            if ((fileSaveTimeStamp + (oldFileAgeLimit * TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS))) >= currentTime) {
                continue;
            }

            amountDeleted++;
            toDelete.add(file);
        }

        if (toDelete.isEmpty()) {
            return;
        }

        pluginRef.getLogger().info("Cleaned backup files. Deleted " + amountDeleted + " of " + amountTotal + " files.");

        for (File file : toDelete) {
            if (file.delete()) {
                pluginRef.debug("Deleted: " + file.getName());
            }
        }
    }

    /**
     * Check if date is within last 24 hours
     *
     * @param date date to check
     * @return true is date is within last 24 hours, false if otherwise
     */
    private boolean isPast24Hours(Date date) {
        Date modifiedDate = new Date(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS));
        return date.after(modifiedDate);
    }

    /**
     * Check if date is within the last week
     *
     * @param date date to check
     * @return true is date is within the last week, false if otherwise
     */
    private boolean isLastWeek(Date date) {
        Date modifiedDate = new Date(System.currentTimeMillis() - TimeUnit.MILLISECONDS.convert(7, TimeUnit.DAYS));
        return date.after(modifiedDate);
    }

    private Date getDate(String fileName) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Date date;

        try {
            date = dateFormat.parse(fileName);
        } catch (ParseException e) {
            return null;
        }

        return date;
    }
}
