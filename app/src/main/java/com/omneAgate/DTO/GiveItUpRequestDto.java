package com.omneAgate.DTO;

import android.database.Cursor;

import com.omneAgate.Util.Constants.FPSDBConstants;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class GiveItUpRequestDto extends BaseDto implements Serializable{

	/** serialversionId of the serializable */
	private static final long serialVersionUID = 1L;
	/**auto generated*/

	/** Primary Key column. */
	private Long id;
	
	/** reference of the beneficiary entity */
	private BeneficiaryDto beneficiaryDto;
	
	/** random reference id this record */
	String referenceNo;
	
	private String channel;
	
	/** request start date */
	private long startDate;
	
	/** request end date */
	private long endDate;
	
	/**this request is valid for all upcoming months*/
	private boolean forever;
	
	/** created on date and time */
	private long createdDate;
	
	/** modified on date and time */
	private long modifiedDate;
	
	/**status of the request */
	private boolean revokeStatus;
	
	/**list of give it up request detail*/
	private List<GiveItUpRequestDetailDto> giveItUpRequestDetailDtoList;

	public GiveItUpRequestDto(Cursor cursor) {
		id = cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_KEY_ID));
		referenceNo = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_REFERENCE_NO));
		channel = cursor.getString(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_CHANNEl));
		startDate = cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_START_DATE));
		endDate = cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_END_DATE));
		int forever = cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_FOREVER));
		this.forever = forever == 0;
		createdDate = cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_CREATED_DATE));
		modifiedDate = cursor.getLong(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_MODIFIED_DATE));
		int revokeStatus = cursor.getInt(cursor.getColumnIndex(FPSDBConstants.KEY_GIVEITUP_REVOKE_STATUS));
		this.revokeStatus = revokeStatus == 0;
	}
}	