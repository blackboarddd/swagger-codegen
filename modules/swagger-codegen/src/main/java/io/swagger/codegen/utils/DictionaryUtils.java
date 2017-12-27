package io.swagger.codegen.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import io.swagger.codegen.AbstractGenerator;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.annotations.ApiModel;

public class DictionaryUtils {
	private static String dictPath = "Z://Yati/Dictionaries/Java.ydic";
	private static DictionaryUtils dictionaryUtils = null;
	private HashMap<String, Variable> allExistingVariables;
	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGenerator.class);
	private static Document document;

	private DictionaryUtils() {
		
	}

	public static DictionaryUtils getDictionaryUtils() {
		if (dictionaryUtils == null) {
			dictionaryUtils = new DictionaryUtils();
		}
		return dictionaryUtils;
	}

	public void writeToDictionary(DefaultGenerator dg, Map<String, Object> templateData) throws IOException, DocumentException {
		String variableXML = getContents(dg, templateData, "variables.mustache");		
		String componentXML = getContents(dg, templateData, "component.mustache");
		Element componenetElement = DocumentHelper.parseText(componentXML).getRootElement().createCopy();
		
		List<Element> elementList = document.getRootElement().elements("module");
		for(Element element: elementList) {
			if(element.attribute("name").getData().equals("Rest Api")) {
				element.add(componenetElement);
				if(null != variableXML) {
					List<Element> variableElements = DocumentHelper.parseText(variableXML).getRootElement().elements("variable");
					Element variableElement = element.element("variables");
					for(Element vElement : variableElements) {
						variableElement.add(vElement.createCopy());
					}
				}
			}
		}		
         XMLWriter writer = new XMLWriter(new FileWriter(dictPath)); 
         writer.write(document); 
         writer.close();
	}
	
	
	//Get data parsed by mustache
	private String getContents(DefaultGenerator dg, Map<String, Object> templateData, String templateName) throws IOException {
		String templateFile = dg.getFullTemplateFile(dg.config, templateName); 
		String template = dg.readTemplate(templateFile);
		Mustache.Compiler compiler = Mustache.compiler();
		compiler = dg.config.processCompiler(compiler);
		Template tmpl = compiler.withLoader(new Mustache.TemplateLoader() {
			@Override
			public Reader getTemplate(String name) {
				return dg.getTemplateReader(dg.getFullTemplateFile(dg.config, name + ".mustache"));
			}
		}).defaultValue("").compile(template);
		return tmpl.execute(templateData);
	}

	/**
	 * Process operations data to make it easier use for dictionary
	 * @param operation
	 * @return
	 */
	public Map<String, Object> processFromOperation(Map<String, Object> operation, String moduleFolder) {
		document = readXML(dictPath);
		allExistingVariables = storeVariablesInRestApi();
		HashMap<String, Object> mapOperations = (HashMap<String, Object>) operation.get("operations");
		CodegenOperation co = (CodegenOperation) mapOperations.get("operation");
		co.dictAbort = generateGUID();
		co.dictOk = generateGUID();
		co.dictFail = generateGUID();
		co.dictId = generateGUID();
		List<CodegenParameter> listParams = co.allParams;
		for (CodegenParameter cp : listParams) {
			cp = transferDataType(cp);
			cp = verifyVariableExisting(cp, allExistingVariables);
		}
		removeExistingComponet(co);
		if(null != co.returnType) {
			CodegenParameter returnParam = constructReturnParam(co, moduleFolder + "/" + co.returnType + ".java");
			returnParam = transferDataType(returnParam);
			returnParam = verifyVariableExisting(returnParam, allExistingVariables);
			co.returnParam = returnParam;
		}
		return operation;
	}
	
	private CodegenParameter constructReturnParam(CodegenOperation co, String moduleFile) {
		CodegenParameter returnParam = new CodegenParameter();
		returnParam.dataType = co.returnType;
		returnParam.paramName = "rest" + co.returnType;
		returnParam.description = getDescription(co, moduleFile);
		return returnParam;
				
	}
	
	private String getDescription(CodegenOperation co, String moduleFile) {
		String tempString = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(moduleFile)));			
			while (( tempString = reader.readLine()) != null) {  
               if(tempString.contains("@ApiModel(description")) {
            	   tempString = tempString.replace("@ApiModel(description = ", "").replace(")", "");
            	   break;
               }
            }  
            reader.close(); 
            return tempString;
		} catch (Exception e) {
		}
		return tempString;
	}

	/**
	 * Verify whether variable exists or not
	 * 
	 * @param cp
	 * @param allExistingVariables
	 * @return
	 */
	private CodegenParameter verifyVariableExisting(CodegenParameter cp,
			HashMap<String, Variable> allExistingVariables) {
		String name = cp.paramName;
		if (allExistingVariables.containsKey(name)) {
			if (cp.dictType.equals(allExistingVariables.get(name).getType())) {
				cp.dictId = allExistingVariables.get(name).getId();
				return cp;
			}
		}
		cp.dictIdForCreation = generateGUID();
		cp.dictId = cp.dictIdForCreation;
		return cp;
	}

	/**
	 * Transfer dataType to available types in dictionary
	 * 
	 * @param codegenParameter
	 * @return
	 */
	private CodegenParameter transferDataType(CodegenParameter codegenParameter) {
		if (codegenParameter.isString) {
			codegenParameter.dictType = "string";
		} else if (codegenParameter.isBoolean) {
			codegenParameter.dictType = "boolean";
		} else if (codegenParameter.isInteger) {
			codegenParameter.dictType = "integer";
		} else if (codegenParameter.isListContainer) {
			codegenParameter.dictType = "array";
		} else {
			codegenParameter.dictType = "object";
		}
		return codegenParameter;
	}

	private Document readXML(String path) {
		SAXReader reader = new SAXReader();
		try {
			document = reader.read(new FileInputStream(new File(path)));
			return document;
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Get all variables in Rest Api module
	 * 
	 * @return
	 */
	private HashMap<String, Variable> storeVariablesInRestApi() {
		HashMap<String, Variable> map = new HashMap<String, Variable>();
		document = readXML(dictPath);
		List<Node> list = document.selectNodes("//module[@name = 'Rest Api']/variables/variable");
		for (Node node : list) {
			String name = node.valueOf("@name");
			String id = node.valueOf("@id");
			String type = node.valueOf("@type");
			if (map.containsKey(name)) {
				if (map.get(name).getType().equals(type)) {
					break;
				}
			}
			map.put(name, new DictionaryUtils.Variable(id, name, type));
		}
		return map;
	}
	
	private Document removeExistingComponet(CodegenOperation co) {
		String actionName = co.operationIdCamelCase + "Action";
		String xpath = "//module[@name = 'Rest Api']/component[@name='" + actionName + "']";
		Node node = document.selectSingleNode(xpath);
		if(null != node) {
			node.getParent().remove(node);
		}
		return document;
	}

	/**
	 * generate GUID
	 * 
	 * @return
	 */
	private String generateGUID() {
		return UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
	}

	public static void main(String[] args) {
		DictionaryUtils dUtils = new DictionaryUtils();
		System.out.println(System.currentTimeMillis());
		HashMap<String, Variable> test = dUtils.storeVariablesInRestApi();
		System.out.println(System.currentTimeMillis());
		Iterator<Entry<String, Variable>> iterator = test.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Variable> entry = iterator.next();
			Variable variable = entry.getValue();
			System.out.println(variable.id + " " + variable.name + " " + variable.type);
		}
		// System.out.println(DictionaryUtils.generateGUID());
	}

	class Variable {
		public Variable(String id, String name, String type) {
			this.id = id;
			this.name = name;
			this.type = type;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		private String id;
		private String name;
		private String type;
	}
}