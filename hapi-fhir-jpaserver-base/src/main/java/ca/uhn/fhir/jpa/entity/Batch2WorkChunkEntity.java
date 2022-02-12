package ca.uhn.fhir.jpa.entity;

import ca.uhn.fhir.batch2.model.StatusEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

import static ca.uhn.fhir.batch2.model.JobDefinition.ID_MAX_LENGTH;
import static ca.uhn.fhir.jpa.entity.Batch2JobInstanceEntity.STATUS_MAX_LENGTH;
import static org.apache.commons.lang3.StringUtils.left;

@Entity
@Table(name = "BT2_WORK_CHUNK", indexes = {
	@Index(name = "IDX_BT2WC_II_SEQ", columnList = "INSTANCE_ID,SEQ")
})
public class Batch2WorkChunkEntity implements Serializable {

	private static final long serialVersionUID = -6202771941965780558L;
	private static final int ERROR_MSG_MAX_LENGTH = 500;

	@Id
	@Column(name = "ID", length = ID_MAX_LENGTH)
	private String myId;
	@Column(name = "SEQ", nullable = false)
	private int mySequence;
	@Column(name = "CREATE_TIME", nullable = false)
	private Date myCreateTime;
	@Column(name = "START_TIME", nullable = true)
	private Date myStartTime;
	@Column(name = "END_TIME", nullable = true)
	private Date myEndTime;
	@Column(name = "RECORDS_PROCESSED", nullable = true)
	private Integer myRecordsProcessed;
	@Column(name = "DEFINITION_ID", length = ID_MAX_LENGTH, nullable = false)
	private String myJobDefinitionId;
	@Column(name = "DEFINITION_VER", length = ID_MAX_LENGTH, nullable = false)
	private int myJobDefinitionVersion;
	@Column(name = "TGT_STEP_ID", length = ID_MAX_LENGTH, nullable = false)
	private String myTargetStepId;
	@Lob
	@Column(name = "DATA", nullable = true, length = Integer.MAX_VALUE - 1)
	private String mySerializedData;
	@Column(name = "STAT", length = STATUS_MAX_LENGTH, nullable = false)
	@Enumerated(EnumType.STRING)
	private StatusEnum myStatus;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "INSTANCE_ID", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "FK_BT2WC_INSTANCE"))
	private Batch2JobInstanceEntity myInstance;
	@Column(name = "INSTANCE_ID", length = ID_MAX_LENGTH, nullable = false)
	private String myInstanceId;
	@Column(name = "ERROR_MSG", length = ERROR_MSG_MAX_LENGTH, nullable = true)
	private String myErrorMessage;

	public String getErrorMessage() {
		return myErrorMessage;
	}

	public void setErrorMessage(String theErrorMessage) {
		myErrorMessage = left(theErrorMessage, ERROR_MSG_MAX_LENGTH);
	}

	public int getSequence() {
		return mySequence;
	}

	public void setSequence(int theSequence) {
		mySequence = theSequence;
	}

	public Date getCreateTime() {
		return myCreateTime;
	}

	public void setCreateTime(Date theCreateTime) {
		myCreateTime = theCreateTime;
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

	public Integer getRecordsProcessed() {
		return myRecordsProcessed;
	}

	public void setRecordsProcessed(Integer theRecordsProcessed) {
		myRecordsProcessed = theRecordsProcessed;
	}

	public Batch2JobInstanceEntity getInstance() {
		return myInstance;
	}

	public void setInstance(Batch2JobInstanceEntity theInstance) {
		myInstance = theInstance;
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

	public String getTargetStepId() {
		return myTargetStepId;
	}

	public void setTargetStepId(String theTargetStepId) {
		myTargetStepId = theTargetStepId;
	}

	public String getSerializedData() {
		return mySerializedData;
	}

	public void setSerializedData(String theSerializedData) {
		mySerializedData = theSerializedData;
	}

	public StatusEnum getStatus() {
		return myStatus;
	}

	public void setStatus(StatusEnum theStatus) {
		myStatus = theStatus;
	}

	public String getId() {
		return myId;
	}

	public void setId(String theId) {
		myId = theId;
	}

	public String getInstanceId() {
		return myInstanceId;
	}

	public void setInstanceId(String theInstanceId) {
		myInstanceId = theInstanceId;
	}
}