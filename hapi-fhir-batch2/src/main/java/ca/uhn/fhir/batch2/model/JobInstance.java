package ca.uhn.fhir.batch2.model;

import ca.uhn.fhir.model.api.IModelJson;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JobInstance implements IModelJson {

	@JsonProperty(value = "jobDefinitionId")
	private String myJobDefinitionId;

	@JsonProperty(value = "jobDefinitionVersion")
	private int myJobDefinitionVersion;

	@JsonProperty(value = "instanceId", access = JsonProperty.Access.READ_ONLY)
	private String myInstanceId;

	@JsonProperty(value = "status")
	private StatusEnum myStatus;

	@JsonProperty(value = "parameters")
	private List<JobInstanceParameter> myParameters;

	@JsonProperty(value = "createTime")
	private Date myCreateTime;

	@JsonProperty(value = "startTime")
	private Date myStartTime;

	@JsonProperty(value = "endTime")
	private Date myEndTime;

	@JsonProperty(value = "combinedRecordsProcessed")
	private Integer myCombinedRecordsProcessed;

	@JsonProperty(value = "combinedRecordsProcessedPerSecond")
	private Integer myCombinedRecordsProcessedPerSecond;

	@JsonProperty(value = "totalElapsedMillis")
	private Integer myTotalElapsedMillis;

	@JsonProperty(value = "workChunksPurged", access = JsonProperty.Access.READ_ONLY)
	private boolean myWorkChunksPurged;

	@JsonProperty(value = "progress", access = JsonProperty.Access.READ_ONLY)
	private double myProgress;

	public boolean isWorkChunksPurged() {
		return myWorkChunksPurged;
	}

	public void setWorkChunksPurged(boolean theWorkChunksPurged) {
		myWorkChunksPurged = theWorkChunksPurged;
	}

	public List<JobInstanceParameter> getParameters() {
		if (myParameters == null) {
			myParameters = new ArrayList<>();
		}
		return myParameters;
	}

	public StatusEnum getStatus() {
		return myStatus;
	}

	public void setStatus(StatusEnum theStatus) {
		myStatus = theStatus;
	}

	public String getJobDefinitionId() {
		return myJobDefinitionId;
	}

	public void setJobDefinitionId(String theJobDefinitionId) {
		myJobDefinitionId = theJobDefinitionId;
	}

	public int getJobDefinitionVersion() {
		return myJobDefinitionVersion;
	}

	public void setJobDefinitionVersion(int theJobDefinitionVersion) {
		myJobDefinitionVersion = theJobDefinitionVersion;
	}

	public String getInstanceId() {
		return myInstanceId;
	}

	public void setInstanceId(String theInstanceId) {
		myInstanceId = theInstanceId;
	}

	public void addParameter(@Nonnull JobInstanceParameter theParameter) {
		Validate.notNull(theParameter);
		getParameters().add(theParameter);
	}

	public Date getStartTime() {
		return myStartTime;
	}

	public void setStartTime(Date theStartTime) {
		myStartTime = theStartTime;
	}

	public Date getEndTime() {
		return myEndTime;
	}

	public void setEndTime(Date theEndTime) {
		myEndTime = theEndTime;
	}

	public Integer getCombinedRecordsProcessed() {
		return myCombinedRecordsProcessed;
	}

	public void setCombinedRecordsProcessed(Integer theCombinedRecordsProcessed) {
		myCombinedRecordsProcessed = theCombinedRecordsProcessed;
	}

	public Integer getCombinedRecordsProcessedPerSecond() {
		return myCombinedRecordsProcessedPerSecond;
	}

	public void setCombinedRecordsProcessedPerSecond(Integer theCombinedRecordsProcessedPerSecond) {
		myCombinedRecordsProcessedPerSecond = theCombinedRecordsProcessedPerSecond;
	}

	public Date getCreateTime() {
		return myCreateTime;
	}

	public void setCreateTime(Date theCreateTime) {
		myCreateTime = theCreateTime;
	}

	public Integer getTotalElapsedMillis() {
		return myTotalElapsedMillis;
	}

	public void setTotalElapsedMillis(Integer theTotalElapsedMillis) {
		myTotalElapsedMillis = theTotalElapsedMillis;
	}

	public void setProgress(double theProgress) {
		myProgress = theProgress;
	}

	public double getProgress() {
		return myProgress;
	}
}