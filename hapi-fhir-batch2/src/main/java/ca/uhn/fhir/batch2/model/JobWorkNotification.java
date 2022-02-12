package ca.uhn.fhir.batch2.model;

import ca.uhn.fhir.model.api.IModelJson;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JobWorkNotification implements IModelJson {

	@JsonProperty(value = "jobDefinitionId")
	private String myJobDefinitionId;

	@JsonProperty(value = "jobDefinitionVersion")
	private int myJobDefinitionVersion;

	@JsonProperty(value = "targetStepId")
	private String myTargetStepId;

	@JsonProperty(value = "chunkId")
	private String myChunkId;

	@JsonProperty(value = "instanceId")
	private String myInstanceId;

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

	public String getTargetStepId() {
		return myTargetStepId;
	}

	public void setTargetStepId(String theTargetStepId) {
		myTargetStepId = theTargetStepId;
	}

	public String getChunkId() {
		return myChunkId;
	}

	public void setChunkId(String theChunkId) {
		myChunkId = theChunkId;
	}

	public void setInstanceId(String theInstanceId) {
		myInstanceId = theInstanceId;
	}

	public String getInstanceId() {
		return myInstanceId;
	}
}