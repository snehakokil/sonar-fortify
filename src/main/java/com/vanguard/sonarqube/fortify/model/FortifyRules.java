package com.vanguard.sonarqube.fortify.model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.codehaus.staxmate.SMInputFactory;
import org.codehaus.staxmate.in.SMEvent;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import com.vanguard.sonarqube.fortify.model.xml.FortifyAssert;
import com.vanguard.sonarqube.fortify.model.xml.FortifyXMLParseException;

public class FortifyRules implements RulesDefinition {
	private static final Logger LOG = Loggers.get(FortifyRules.class);
	
	private RulesDefinitionXmlLoader loader;
	
	public FortifyRules(RulesDefinitionXmlLoader xmlloader) {
		this.loader = xmlloader;
	}

	/* (non-Javadoc)
	 * @see org.sonar.api.server.rule.RulesDefinition#define(org.sonar.api.server.rule.RulesDefinition.Context)
	 * 
	 * Create a repository for each language, so that duplicate rules will be stored in separate namespaces.
	 */
	public void define(Context c) {
		NewRepository repo = c.createRepository(FortifyConstants.JAVA_REPOSITORY_KEY, FortifyConstants.LANGUAGE_KEY);
		repo.setName("Fortify Java Repository");
		this.loader.load(repo, new InputStreamReader(getClass().getResourceAsStream(FortifyConstants.JAVA_RULE_FILE)));
		repo.done();
		
		repo = c.createRepository(FortifyConstants.JS_REPOSITORY_KEY, FortifyConstants.LANGUAGE_KEY);
		repo.setName("Fortify JavaScript Repository");
		this.loader.load(repo, new InputStreamReader(getClass().getResourceAsStream(FortifyConstants.JS_RULE_FILE)));
		repo.done();
		
		repo = c.createRepository(FortifyConstants.XML_REPOSITORY_KEY, FortifyConstants.LANGUAGE_KEY);
		repo.setName("Fortify XML Repository");
		this.loader.load(repo, new InputStreamReader(getClass().getResourceAsStream(FortifyConstants.XML_RULE_FILE)));
		repo.done();
	}
	
	/**
	 * This function returns an array of strings ordered by the likelihood that the file name
	 * denoted by {@code fileName} uses the language of a given repository. This is necessary because
	 * each language rule set is stored in its own repository. Thus, this application must make an
	 * assumption of the language of the source code, which it does with file extension comparisons.
	 * 
	 * @param fileName The name of the source file in question, which must include the extension.
	 * @return An ordered array of repository name strings where the most likely repository given a
	 * 			file name is the first element of the array.
	 */
	public static String[] selectRepositoryFromFileName(String fileName) {
		List<String> repos = new ArrayList<String>();
		repos.addAll(Arrays.asList(FortifyConstants.REPOSITORY_KEYS));
		for (Entry<String, String> ext:FortifyConstants.EXT_MAP.entrySet()) {
			if (fileName.endsWith(ext.getKey())) {
				repos.remove(ext.getValue());
				repos.add(0, ext.getValue());
				break;
			}
		}
		return repos.toArray(new String[FortifyConstants.REPOSITORY_KEYS.length]);
	}

	/**
	 * This function assists by helping activate all applicable rules in the new fortify quality profile. Given the rule names,
	 * the script can then ensure that the rule isn't overridden and subsequently activate it if desired. The rule-packs to use
	 * with this function should be accessible as resources under the project root: {@code getClass().getResourceAsStream("/something.xml")}.
	 * 
	 * @param in An {@link InputStream} object which emits the rule-pack XML data.
	 * @return A map of rule names to their keys. No values may be null.
	 */
	public static Map<String, String> parseRuleNameKeyMap(InputStream in) {
		Map<String, String> nameToKeyMap = new HashMap<String, String>();
		
		SMInputFactory fact = SMInputFactory.getGlobalSMInputFactory();
		SMHierarchicCursor root_cursor;
		try {
			root_cursor = fact.rootElementCursor(in);
			
			SMEvent evt = root_cursor.getNext();

			FortifyAssert.True(evt == SMEvent.START_ELEMENT);
			FortifyAssert.True(root_cursor.getLocalName().equals("rules"));
			
			SMHierarchicCursor cursor = (SMHierarchicCursor) root_cursor.childElementCursor();
			
			while ((cursor.getNext()) != null) {
				FortifyAssert.True(cursor.getLocalName().equals("rule"));
				
				SMInputCursor rule_cursor = cursor.childElementCursor();
				String key = null;
				String name = null;
				while ((rule_cursor.getNext()) != null) {
					String rule_tag = rule_cursor.getLocalName();
					if (rule_tag.equals("key")) {
						key = rule_cursor.getElemStringValue();
					} else if (rule_tag.equals("name")) {
						name = rule_cursor.getElemStringValue();
					}
					if (key != null && name != null) break;
				}
				FortifyAssert.True(key != null && name != null);
				
				nameToKeyMap.put(name, key);
			}
		} catch (XMLStreamException e) {
			LOG.error("Failed to stream fortify rule name key map", e);
		} catch (FortifyXMLParseException e) {
			LOG.error("Failed to parse fortify rule name key map", e);
		}
		return nameToKeyMap;
	}
}
