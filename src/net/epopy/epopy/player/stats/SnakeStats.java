package net.epopy.epopy.player.stats;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import net.epopy.epopy.utils.Config;

public class SnakeStats {

	private int pts;
	private Config config;
	private int parties;
	private long tempsLong;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");;

	public SnakeStats(Config config) {
		this.config = config;
		pts = Integer.parseInt(config.getData("snake_pts"));
		parties = Integer.parseInt(config.getData("snake_parties"));

		tempsLong = Long.parseLong(config.getData("snake_temps"));
	}

	public String getTemps() {
		if (tempsLong <= 0)
			return "00:00:00";
	/**	timeFormat = new SimpleDateFormat("HH'h ' m'min' s'sec'");
		String time = timeFormat.format(tempsLong - 3600000);
		if(time.contains("00h")) time = time.replace("00h", "");
		if(time.contains("00min")) time = time.replace("00min", "");
		if(time.contains("00sec")) time = time.replace("00sec", "");*/
		return timeFormat.format(tempsLong - 3600000);
	}

	public void addTemps(long start) {
		long l = Calendar.getInstance().getTimeInMillis() - start;
		tempsLong += l;
		config.setValue("snake_temps", String.valueOf(tempsLong));
	}

	public int getObjectif() {
		return 100;
	}

	public int getParties() {
		return parties;
	}

	public void addPartie() {
		parties = parties + 1;
		config.setValue("snake_parties", String.valueOf(parties));
	}

	public int getRecord() {
		return pts;
	}

	public void setRecord(int pts) {
		this.pts = pts;
		config.setValue("snake_pts", String.valueOf(pts));
	}
}
