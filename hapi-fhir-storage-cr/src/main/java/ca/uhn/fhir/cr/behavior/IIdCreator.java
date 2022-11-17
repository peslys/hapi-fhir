package ca.uhn.fhir.cr.behavior;

/*-
 * #%L
 * HAPI FHIR - Clinical Reasoning
 * %%
 * Copyright (C) 2014 - 2022 Smile CDR, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.cr.utility.Ids;
import org.hl7.fhir.instance.model.api.IIdType;

import static com.google.common.base.Preconditions.checkNotNull;


public interface IIdCreator extends IFhirContextUser {

	default <T extends IIdType> T newId(String theResourceName, String theResourceId) {
		checkNotNull(theResourceName);
		checkNotNull(theResourceId);

		return Ids.newId(getFhirContext(), theResourceName, theResourceId);
	}

	default <T extends IIdType> T newId(String theResourceId) {
		checkNotNull(theResourceId);

		return Ids.newId(getFhirContext(), theResourceId);
	}
}