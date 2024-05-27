package fr.yukina.game.profiling.writer;

import fr.yukina.game.profiling.Profiler;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

@Getter
public class ProfilerCSVWriter implements IProfilerWriter
{
	private static final Logger logger = Logger.getLogger(ProfilerCSVWriter.class.getName());

	private final   Profiler    profiler;
	private final   String      filePath;
	private         PrintWriter writer;
	private @Setter boolean     indexed;
	private @Setter boolean     temporal;

	public ProfilerCSVWriter(Profiler profilerIn, final String filePathIn)
	{
		this.profiler = profilerIn;
		this.filePath = filePathIn;
		try
		{
			this.writer = new PrintWriter(new FileOutputStream(filePathIn, false));
		}
		catch (FileNotFoundException eIn)
		{
			logger.severe("File not found: " + eIn.getMessage());
			throw new RuntimeException(eIn);
		}

		this.indexed  = true;
		this.temporal = true;
	}

	public final void writeHeader()
	{
		this.writer.println("CSV GLOBAL INFORMATIONS:");
		this.writer.println("time unit,CSV description,indexed,temporal");
		this.writer.println("nanoseconds,this document provides profiling metric information," + this.indexed() + ","
		                    + this.temporal());
		this.writer.println();
		this.writer.println("CSV INFORMATIONS:");
		this.writer.println("information");
		if (this.temporal)
		{
			this.writer.println("- This document uses temporal data. the \"duration\" columns specify the duration "
			                    + "during which the value remained the same.");
		}
		this.writer.println();
		this.writer.println();

		this.writer.println("TABLES:");
		this.writer.println("name,description");
		if (this.indexed)
		{
			this.writer.println("INDEX,mapping between index and a profile name");
		}
		this.writer.println("COLUMNS INFORMATIONS,informations about next table columns (for each tables)");
		this.writer.println("METRICS,informations about metrics");
		this.writer.println("AVERAGE,average measure data");
		this.writer.println("SNAPSHOTS,snapshots measure data");
	}

	public final void writeColumnsDescription(String... columnsDescriptionsIn)
	{
		String columnsNames        = "";
		String columnsDescriptions = "";
		for (int i = 0; i < columnsDescriptionsIn.length; i += 2)
		{
			if (i > 0)
			{
				columnsNames += ",";
				columnsDescriptions += ",";
			}
			columnsNames += columnsDescriptionsIn[i];
			columnsDescriptions += columnsDescriptionsIn[i + 1];
		}

		this.writer.println(columnsNames);
		this.writer.println(columnsDescriptions);
		this.writer.println();
	}

	public final void writeData(String... columnsIn)
	{
		int i = 0;
		for (String columnIn : columnsIn)
		{
			if (i > 0)
			{
				this.writer.print(",");
			}
			this.writer.print(columnIn);
			i++;
		}
		this.writer.println();
	}

	public final void writeMetrics()
	{
		if (this.profiler.metricManager().metricInfoMap().isEmpty())
		{
			return;
		}

		this.writer.println("METRICS-COLUMNS:");
		this.writeColumnsDescription("name", "name of metric", "description", "description of metric", "unit",
		                             "unit of metric");
		this.writer.println("METRICS:");
		this.writeData("name,description,unit");
		for (var metricEntry : this.profiler.metricManager().metricInfoMap().entrySet())
		{
			var name        = metricEntry.getKey();
			var description = metricEntry.getValue().description();
			var unit        = metricEntry.getValue().unit();
			this.writeData(name, description, unit);
		}
		this.writer.println();
		this.writer.println();
	}

	public final void writeIndex()
	{
		if (!this.indexed)
		{
			return;
		}

		this.writer.println("INDEX-COLUMNS:");
		this.writeColumnsDescription("name", "name of profile", "index", "associated index unique number");
		this.writer.println("INDEX:");
		this.writeData("name,index");
		for (var indexEntry : this.profiler.profilesKeyManager().indexMap().entrySet())
		{
			this.writeData(indexEntry.getKey(), "" + indexEntry.getValue());
		}
		this.writer.println();
		this.writer.println();
	}

	public final void writeAverages()
	{
		this.writer.println("AVERAGE-COLUMNS:");
		this.writeColumnsDescription((this.indexed ? "index" : "profile name"),
		                             (this.indexed ? "profile index" : "profile name"), "metric name", "metric name",
		                             "total value", "accumulated value", "total time", "accumulated time",
		                             "average " + "value", "average value (total value / total time)");
		this.writer.println("AVERAGE:");
		this.writeData(
				(this.indexed ? "index" : "profile name") + ",metric name,total value,total time,average " + "value");
		for (var profile : this.profiler.profileManager().profileMap().values())
		{
			var key = this.profiler.getProfileKey(this.indexed, profile.name());
			for (var metricDataManager : profile.snapshotManager().metricDataManagerMap().values())
			{
				var metricName         = metricDataManager.name();
				var metricTotalValue   = "" + metricDataManager.totalValue();
				var metricTotalTime    = "" + metricDataManager.totalTime();
				var metricAverageValue = "" + metricDataManager.averageValue();
				if (!this.profiler.metricManager().metricInfoMap().containsKey(metricName))
				{
					continue;
				}

				this.writeData(key, metricName, metricTotalValue, metricTotalTime, metricAverageValue);
			}
		}
		this.writer.println();
		this.writer.println();
	}

	public void writeSnapshots()
	{
		this.writer.println("SNAPSHOTS-COLUMNS:");
		this.writeColumnsDescription((this.indexed ? "index" : "profile name"),
		                             (this.indexed ? "profile index" : "profile name"), "metric name", "metric name",
		                             (this.temporal ? "duration" : "cpu start" + "time"), (this.temporal
		                                                                                   ? "duration during "
		                                                                                     + "which the value is "
		                                                                                     + "the same"
		                                                                                   : "cpu start time in "
		                                                                                     + "nanoseconds"),
		                             "metric value", "metric snapshot value");
		this.writer.println("SNAPSHOTS:");
		this.writeData(
				(this.indexed ? "index" : "profile name") + ",metric name," + (this.temporal ? "duration" : "cpu time"),
				"metric value");
		for (var profile : this.profiler.profileManager().profileMap().values())
		{
			var key = this.profiler.getProfileKey(this.indexed, profile.name());

			for (var metricDataManager : profile.snapshotManager().metricDataManagerMap().values())
			{
				var metricName = metricDataManager.name();
				for (var metricSnapshot : metricDataManager.metricDataSnapshotMap())
				{
					var time = this.temporal ? metricSnapshot.duration() : metricSnapshot.startTime();
					System.out.println("time: " + time);
					var value = metricSnapshot.value();
					this.writeData(key, metricName, "" + time, "" + value);
				}
			}
		}
		this.writer.println();
		this.writer.println();
	}

	@Override
	public final void write()
	{
		this.writeHeader();
		this.writeMetrics();
		this.writeIndex();
		this.writeAverages();
		this.writeSnapshots();

		this.writer.flush();
		this.writer.close();
	}
}