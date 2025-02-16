package org.bridgedb.webservicetesting.BridgeDbWebservice;

import java.util.Map;
import org.bridgedb.IDMapperStack;
import org.bridgedb.Xref;
import org.json.simple.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

public class AttributeSearch extends RestletResource {
	String searchStr;
	String attribute;
	int limit = 0;

	protected void doInit() throws ResourceException {
		super.doInit();
		try {
			this.searchStr = getAttribute(RestletService.PAR_QUERY);
			this.attribute = getAttribute(RestletService.PAR_TARGET_ATTR_NAME);
			String limitStr = getAttribute(RestletService.PAR_TARGET_LIMIT);
			System.out.println("limit string:"+ limitStr);
			System.out.println("attribute string:"+ attribute);
			
			if (null != limitStr) {
				this.limit = new Integer(limitStr).intValue();
			}
		} catch (Exception e) {
			throw new ResourceException(e);
		}
	}

	@Get("json")
	public Representation get(Variant variant) {
    	if (!supportedOrganism(urlDecode((String) getRequest().getAttributes().get(RestletService.PAR_ORGANISM)))) {
			String error = UNSUPPORTED_ORGANISM_TEMPLATE.replaceAll("%%ORGANISM%%", (String) getRequest().getAttributes().get(RestletService.PAR_ORGANISM));
			StringRepresentation sr = new StringRepresentation(error);
			sr.setMediaType(MediaType.TEXT_HTML);
			return sr;
    	}
		try {
			IDMapperStack stack = getIDMappers();
			if (attribute == null) attribute = "Symbol"; // use symbol by default.
			Map<Xref, String> results = stack.freeAttributeSearch(searchStr, attribute, limit);

			if (MediaType.APPLICATION_JSON.isCompatible(variant.getMediaType())) {
				JSONObject jsonObject = new JSONObject();
				JSONObject attributeSearchResult = new JSONObject();
				for (Xref x : results.keySet()) {
					attributeSearchResult.put("full name: ", x.getDataSource().getFullName());
					attributeSearchResult.put("xref: ", results.get(x));
					jsonObject.put(x.getId(),attributeSearchResult);
				}
				
				return new StringRepresentation(jsonObject.toString());
			} else {
				StringBuilder result = new StringBuilder();
				for (Xref x : results.keySet()) {
					result.append(x.getId());
					result.append("\t");
					result.append(x.getDataSource().getFullName());
					result.append("\t");
					result.append(results.get(x));
					result.append("\n");
				}
				return new StringRepresentation(result.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return new StringRepresentation(e.getMessage());
		}
	}

}
