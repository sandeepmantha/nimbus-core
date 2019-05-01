
package com.antheminc.oss.nimbus.integration.mq;

import java.io.Serializable;

import lombok.Data;

/**
 * @author Sandeep Mantha
 */
@Data
public class MqMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String commandUrl;
	private String rawPayload;
}
