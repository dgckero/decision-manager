/*
  @author david
 */

package com.dgc.dm.core.service.bpmn;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.service.db.FilterService;
import com.dgc.dm.core.service.db.RowDataService;
import com.dgc.dm.core.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.dmn.engine.*;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelException;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.*;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.camunda.bpm.model.xml.impl.util.IoUtil;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@Transactional
public class BPMNServerImpl implements BPMNServer {
    private static final String DEFINITIONS_NAMESPACE = "http://camunda.org/schema/1.0/dmn";
    private static final String ACCEPTED_AND_SEND_EMAIL = "Accepted_sendEMail";
    private static final String ACCEPTED = "Accepted";
    private static final Collection<String> OMITTED_DATA = Stream.of("rowId", "project").collect(Collectors.toList());
    private static final String OUTPUT_ID = "output1";
    private static final String OUTPUT_LABEL = "rule matched?";
    private static final String STRING_TYPE_REF = "string";
    private static final String INPUT_EXPRESSION_ID = "inputExpression_";
    private static final String SUFIX_DECISION_MANAGER_DM = "decision-manager.dm";
    private static final String DECISION_TABLE_ID = "decisionTable-";
    private static final String DEFINITION_PREFIX = "definition-";
    private static final String DECISION_PREFIX = "decision-";

    @Autowired
    private EmailService emailService;
    @Autowired
    private RowDataService rowDataService;
    @Autowired
    private FilterService filterService;

    /**
     * Create a instance of Definition model
     *
     * @param modelInstance
     * @param definitionName name for new Definition model
     * @param definitionId   id for new Definition model
     * @return instance of Definition model
     */
    private static Definitions createDefinition(DmnModelInstance modelInstance, String definitionName, String definitionId) {
        log.debug("[INIT] createDefinition definitionName: {}", definitionName);
        final Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setNamespace(DEFINITIONS_NAMESPACE);
        definitions.setName(definitionName);
        definitions.setId(definitionId);
        log.debug("[END] createDefinition definitionName: {}", definitionName);
        return definitions;
    }

    /**
     * Create a instance of Definition model
     *
     * @param modelInstance
     * @param decisionId    id for new Decision model
     * @param decisionName  name for new Decision model
     * @return instance of Definition model
     */
    private static Decision createDecision(DmnModelInstance modelInstance, String decisionId, String decisionName) {
        log.debug("[INIT] createDecision decisionName: {}", decisionName);
        final Decision decision = modelInstance.newInstance(Decision.class);
        decision.setId(decisionId);
        decision.setName(decisionName);
        log.debug("[END] createDecision decisionName: {}", decisionName);
        return decision;
    }

    /**
     * Generate DMN File on outputFilePath
     *
     * @param project
     * @param modelInstance
     * @param outputFilePath
     */
    private static void generateDmnFile(ProjectDto project, final DmnModelInstance modelInstance, final String outputFilePath) {
        log.debug("[INIT] generateDmnFile project: {}, outputFilePath: {}", project, outputFilePath);
        final File dmnFile = new File(outputFilePath);
        Dmn.writeModelToFile(dmnFile, modelInstance);
        try {
            project.setDmnFile(IOUtils.toByteArray(new FileInputStream(dmnFile)));
        } catch (FileNotFoundException e) {
            log.error("File not found " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Error parsing file to byte  array {}", e.getMessage());
            e.printStackTrace();
        }
        log.debug("[END] generated dmn file: {}", dmnFile.getAbsolutePath());
    }

    /**
     * Create Output object
     *
     * @param dmnModelInstance
     * @return Output object
     */
    private static Output createOutput(final DmnModelInstance dmnModelInstance) {
        log.debug("[INIT] createOutput");
        final Output output = dmnModelInstance.newInstance(Output.class);
        output.setId(OUTPUT_ID);
        output.setLabel(OUTPUT_LABEL);
        output.setTypeRef(STRING_TYPE_REF);
        log.debug("[END] createOutput :{}", output);
        return output;
    }

    /**
     * Create InputExpression Object
     *
     * @param dmnModelInstance
     * @param name
     * @param filterClass
     * @returnInputExpression Object
     */
    private static ModelElementInstance createInputExpression(final DmnModelInstance dmnModelInstance, final String name, final String filterClass) {
        log.debug("[INIT] createInputExpression name:{}, filterClass: {}", name, filterClass);
        final InputExpression inputExpression = dmnModelInstance.newInstance(InputExpression.class);
        inputExpression.setTypeRef(getTypeRef(filterClass));
        inputExpression.setId(INPUT_EXPRESSION_ID + StringUtils.stripAccents(name));

        final Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(name);
        inputExpression.addChildElement(text);

        log.debug("[END] createInputExpression :{}", inputExpression);
        return inputExpression;
    }

    /**
     * Get typeRef by filterClass
     *
     * @param filterClass
     * @return typeRef by filterClass
     */
    private static String getTypeRef(final String filterClass) {
        log.debug("[INIT] getTypeRef by filterClass: {}", filterClass);
        final String result;
        final String filterLowerClass = filterClass.toLowerCase();
        if (filterLowerClass.equals("date")) {
            result = STRING_TYPE_REF;
        } else {
            result = filterLowerClass;
        }
        log.debug("[END] getTypeRef : {}", result);
        return result;
    }

    /**
     * Create InputEntry object based on filter information
     *
     * @param dmnModelInstance
     * @param filter
     * @return InputEntry object
     */
    private static InputEntry createInputEntry(DmnModelInstance dmnModelInstance, final FilterDto filter) {
        log.debug("[INIT] createInputEntry based on filter: {}", filter);
        final InputEntry inputEntry = dmnModelInstance.newInstance(InputEntry.class);
        inputEntry.setLabel(filter.getName());
        inputEntry.setText(createText(dmnModelInstance, filter));
        log.debug("[END] createInputEntry: {}", inputEntry);
        return inputEntry;
    }

    /**
     * Create Text object with value based on filter
     *
     * @param dmnModelInstance
     * @param filter
     * @return Text object
     */
    private static Text createText(DmnModelInstance dmnModelInstance, final FilterDto filter) {
        log.debug("[INIT] createText based on filter: {}", filter);
        final Text text = dmnModelInstance.newInstance(Text.class);
        final String textContentValue;
        if (Double.class.getSimpleName().equals(filter.getFilterClass())) {
            textContentValue = filter.getValue();
        } else {
            textContentValue = "\"" + filter.getValue() + "\"";
        }
        text.setTextContent(textContentValue);
        log.debug("[END] createText, text: {}", text);
        return text;
    }

    /**
     * Create OutputEntry object
     *
     * @param dmnModelInstance
     * @param expression
     * @param outputEntryId
     * @param outputEntryLabel
     * @return OutputEntry
     */
    private static OutputEntry createOutputEntry(DmnModelInstance dmnModelInstance, String expression, String outputEntryId, String outputEntryLabel) {
        log.debug("[INIT] createOutputEntry expression: {}, outputEntryId: {}, outputEntryLabel: {}", expression, outputEntryId, outputEntryLabel);
        final Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(expression);

        final OutputEntry outputEntry = dmnModelInstance.newInstance(OutputEntry.class);
        outputEntry.setId(outputEntryId);
        outputEntry.setLabel(outputEntryLabel);
        outputEntry.setText(text);
        log.debug("[INIT] createOutputEntry: {}", outputEntry);
        return outputEntry;
    }

    /**
     * Parse Map<String, Object> to VariableMap
     *
     * @param commonEntityMap
     * @return VariableMap
     */
    private static VariableMap parseEntityToVariableMap(Map<String, Object> commonEntityMap) {
        log.debug("[INIT] parseEntityToVariableMap");
        final VariableMap variableToBeValidated = Variables.createVariables();

        final Iterator<Map.Entry<String, Object>> iterator = commonEntityMap.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, Object> commonEntity = iterator.next();

            if (!skipCommonEntity(commonEntity.getKey())) {
                variableToBeValidated.put(commonEntity.getKey(), commonEntity.getValue());
            }
        }
        log.debug("[END] parseEntityToVariableMap: {}", variableToBeValidated);
        return variableToBeValidated;
    }

    /**
     * return true if commonEntityName belongs to OMITTED_DATA
     *
     * @param commonEntityName
     * @return
     */
    private static boolean skipCommonEntity(String commonEntityName) {
        log.debug("[INIT] skipCommonEntity " + commonEntityName);
        boolean skip = false;
        if (OMITTED_DATA.contains(commonEntityName)) skip = true;
        log.debug("[END] skipCommonEntity: {}, result: {}", commonEntityName, skip);
        return skip;
    }

    /**
     * Return true if DmnDecisionRuleResult is equals to ACCEPTED_AND_SEND_EMAIL
     *
     * @param firstResult
     * @return boolean
     */
    private static boolean sendEmail(DmnDecisionRuleResult firstResult) {
        log.debug("[INIT] sendEmail DmnDecisionRuleResult: {}", firstResult);
        boolean sendEmail;
        sendEmail = Objects.equals(firstResult.getFirstEntry(), ACCEPTED_AND_SEND_EMAIL);
        log.debug("[INIT] sendEmail DmnDecisionRuleResult: {}, sendEmail: {}", firstResult, sendEmail);
        return sendEmail;
    }

    /**
     * Return DmnEngine with custom configuration
     *
     * @return DmnEngine
     */
    private static DmnEngine getDmnEngine() {
        log.debug("[INIT] getDmnEngine");
        DefaultDmnEngineConfiguration configuration = (DefaultDmnEngineConfiguration) DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration();
        // add the data type transformer,
        // overriding the existing type "date":
        configuration
                .getTransformer()
                .getDataTypeTransformerRegistry()
                .addTransformer("date", new DecisionManagerDateDataTypeTransformer());

        log.debug("[END] getDmnEngine");
        return configuration.buildEngine();
    }

    /**
     * Create a instance of DecisionTable model
     *
     * @param modelInstance
     * @param decisionTableId
     * @param activeFilters
     * @return instance of DecisionTable model
     */
    private DecisionTable createDecisionTable(DmnModelInstance modelInstance, String decisionTableId, List<FilterDto> activeFilters) {
        log.debug("[INIT] createDecisionTable decisionTableId: {}, activeFilters: {}", decisionTableId, activeFilters);
        final DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
        decisionTable.setId(decisionTableId);
        decisionTable.getInputs().addAll(this.createInputs(modelInstance, activeFilters));
        decisionTable.getOutputs().add(createOutput(modelInstance));
        decisionTable.setHitPolicy(HitPolicy.COLLECT);
        log.debug("[END] createDecisionTable: {}", decisionTable);
        return decisionTable;
    }

    /**
     * Create Input object
     *
     * @param dmnModelInstance
     * @param name
     * @param filterClass
     * @return Input object
     */
    private Input createInput(final DmnModelInstance dmnModelInstance, final String name, final String filterClass) {
        log.debug("[INIT] createInput with name: {}, filterClass: {}", name, filterClass);
        final Input input = dmnModelInstance.newInstance(Input.class);
        input.addChildElement(createInputExpression(dmnModelInstance, name, filterClass));
        log.debug("[END] createInput: {}", input);
        return input;
    }

    /**
     * @param project       project created by user
     * @param activeFilters filters defined by user on decision
     * @param sendMail      if true sends email to contact info of Excel's rows that fit activeFilters
     * @return entities that fit filters defined by user
     * @throws Exception
     */
    @Override
    public final List<Map<String, Object>> createAndRunDMN(ProjectDto project, List<FilterDto> activeFilters, Boolean sendMail) throws Exception {
        log.debug("[INIT] createAndRunDMN for project: {}, sendEmail: {}, activeFilters: {}", project, sendMail, activeFilters);

        final List<Map<String, Object>> result = this.generateDmn(project, project.getName() + SUFIX_DECISION_MANAGER_DM, DECISION_TABLE_ID + project.getName(),
                DEFINITION_PREFIX + project.getId(), DEFINITION_PREFIX + project.getName(), DECISION_PREFIX + project.getId(),
                DECISION_PREFIX + project.getName(), activeFilters, sendMail);

        log.debug("[END] createAndRunDMN");
        return result;
    }

    /**
     * Get list of objects that fits requirements based on activeFilters
     *
     * @param project
     * @param outputFilePath
     * @param decisionTableId
     * @param definitionId
     * @param definitionName
     * @param decisionId
     * @param decisionName
     * @param activeFilters
     * @param sendMail
     * @return list of objects fitting requirements
     * @throws Exception
     */
    private List<Map<String, Object>> generateDmn(ProjectDto project, String outputFilePath,
                                                  String decisionTableId, String definitionId, String definitionName,
                                                  String decisionId, String decisionName, List<FilterDto> activeFilters, Boolean sendMail) throws Exception {
        log.debug("[INIT] generateDmn based on project: {}, outputFilePath: {}, decisionTableId: {}, definitionId: {}, definitionName: {}, decisionId: {}, decisionName: {}, sendMail: {}, activeFilters: {}", project, outputFilePath, decisionTableId, definitionId, definitionName, decisionId, decisionName, sendMail, activeFilters);
        List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();
        try {
            final DmnModelInstance modelInstance = this.createDmnModelInstance(activeFilters, definitionName, definitionId, decisionId, decisionName, decisionTableId, sendMail);

            log.info("Validating model \n{}", IoUtil.convertXmlDocumentToString(modelInstance.getDocument()));
            Dmn.validateModel(modelInstance);
            generateDmnFile(project, modelInstance, outputFilePath);
            commonEntitiesAccepted = this.evaluateDecisionTable(modelInstance, decisionId, project);

            log.debug("[END] generateDmn");
            return commonEntitiesAccepted;
        } catch (final ModelValidationException | DmnTransformException e) {
            log.error("Error generating DMN {}", e.getMessage());
            e.printStackTrace();
            throw new Exception("Error generating DMN " + e.getMessage());
        } catch (final RuntimeException e) {
            log.error("Error {}", e.getMessage());
            e.printStackTrace();
            throw new Exception("Error " + e.getMessage());
        }

    }

    /**
     * Create createDmnModelInstance object
     *
     * @param activeFilters
     * @param definitionName
     * @param definitionId
     * @param decisionId
     * @param decisionName
     * @param decisionTableId
     * @param sendMail
     * @return createDmnModelInstance object
     */
    private DmnModelInstance createDmnModelInstance(List<FilterDto> activeFilters, String definitionName,
                                                    String definitionId, String decisionId, String decisionName,
                                                    String decisionTableId, Boolean sendMail) {
        log.debug("[INIT] createDmnModelInstance based on decisionTableId: {}, definitionId: {}, definitionName: {}, decisionId: {}, decisionName: {}, sendMail: {}, activeFilters: {}", decisionTableId, definitionId, definitionName, decisionId, decisionName, sendMail, activeFilters);

        final DmnModelInstance modelInstance = Dmn.createEmptyModel();

        // Create definition
        final Definitions definitions = createDefinition(modelInstance, definitionName, definitionId);
        modelInstance.setDefinitions(definitions);
        log.debug("Created definition with name{} and id {}", definitionName, definitionId);

        // Create decision
        final Decision decision = createDecision(modelInstance, decisionId, decisionName);
        definitions.addChildElement(decision);
        log.debug("Created decision with name{} and id {}", decisionName, decisionId);

        // Create DecisionTable
        final DecisionTable decisionTable = this.createDecisionTable(modelInstance, decisionTableId, activeFilters);
        decision.addChildElement(decisionTable);
        log.debug("Created decisionTable with id {}", decisionTableId);

        // Create rule
        final Rule rule = this.createRule(modelInstance, activeFilters, sendMail);
        decisionTable.getRules().add(rule);
        log.debug("Created rule with id {}", rule.getId());

        log.debug("[END] createDmnModelInstance successfully created: {}", modelInstance);
        return modelInstance;
    }

    /**
     * Create a list of Input objects based on activeFilters
     *
     * @param dmnModelInstance
     * @param activeFilters
     * @return list of Input objects
     */
    private Collection<? extends Input> createInputs(final DmnModelInstance dmnModelInstance, final List<? extends FilterDto> activeFilters) {
        log.debug("[INIT] createInputs based on activeFilters: {}", activeFilters);
        final List<Input> inputs = new ArrayList<>(activeFilters.size());

        for (final FilterDto filter : activeFilters) {
            log.debug("creating input for filter {}", filter);
            inputs.add(this.createInput(dmnModelInstance, filter.getName(), filter.getFilterClass()));
        }

        log.debug("[END] createInputs");
        return inputs;
    }

    /**
     * Create Rule object
     *
     * @param dmnModelInstance
     * @param activeFilters
     * @param sendMail
     * @return Rule object
     */
    private Rule createRule(DmnModelInstance dmnModelInstance, List<? extends FilterDto> activeFilters, Boolean sendMail) {
        log.debug("[INIT] createRule based on activeFilters: {}, sendMail: {}", activeFilters, sendMail);
        final Rule rule = dmnModelInstance.newInstance(Rule.class);

        for (final FilterDto filter : activeFilters) {
            rule.getInputEntries().add(createInputEntry(dmnModelInstance, filter));
            log.debug("Added inputEntry by filter{}", filter);
        }

        if (null == sendMail || !sendMail) {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"" + ACCEPTED + "\"", ACCEPTED, ACCEPTED));
        } else {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"" + ACCEPTED_AND_SEND_EMAIL + "\"", ACCEPTED_AND_SEND_EMAIL, ACCEPTED_AND_SEND_EMAIL));
        }
        log.debug("[END] createRule result: {}", rule);
        return rule;
    }

    /**
     * Send Email based on contactFilter
     *
     * @param variableToBeValidated
     * @param contactFilter
     * @param project
     */
    private void sendEmail(VariableMap variableToBeValidated, FilterDto contactFilter, ProjectDto project) {
        log.debug("[INIT] sendEmail contactFilter: {}, project: {}", contactFilter, project);
        if (null == contactFilter) {
            log.warn("sendEmail not found filter having contactFilter active");
        } else {
            final String emailTo = ((null == variableToBeValidated.get(contactFilter.getName())) ? null : (String) variableToBeValidated.get(contactFilter.getName()));
            if (null != emailTo) {
                try {
                    log.trace("Sending email to {}", emailTo);
                    this.emailService.sendAsynchronousMail(emailTo, project);
                } catch (final MailException e) {
                    log.error("Error sending email to {}, error: {}", emailTo, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        log.debug("[END] sendEmail");
    }

    /**
     * Evaluate if data project information fits decision
     *
     * @param dmnEngine
     * @param decision
     * @param project
     * @return project information that fits decision
     */
    private List<Map<String, Object>> evaluateEntities(DmnEngine dmnEngine, DmnDecision decision, ProjectDto project) {
        log.debug("[INIT] evaluateEntities for project: {}", project);

        final List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();
        final List<Map<String, Object>> commonEntitiesToBeValidated = this.rowDataService.getRowData(project);
        log.debug("Got {} entities to be validated", commonEntitiesToBeValidated.size());

        final FilterDto contactFilter = this.filterService.getContactFilter(project);

        for (final Map<String, Object> commonEntityMap : commonEntitiesToBeValidated) {
            final VariableMap variableToBeValidated = parseEntityToVariableMap(commonEntityMap);
            final DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variableToBeValidated);
            if (1 <= result.size()) {
                commonEntitiesAccepted.add(variableToBeValidated);
                if (sendEmail(result.getFirstResult())) {
                    log.debug("Result found with email information");
                    this.sendEmail(variableToBeValidated, contactFilter, project);
                }
            }
        }

        log.debug("[END] evaluateEntities for project: {}", project);
        return commonEntitiesAccepted;
    }

    /**
     * Evaluate Project's decision table
     *
     * @param modelInstance
     * @param decisionId
     * @param project
     * @return list of project's information that fits decsion table
     */
    private List<Map<String, Object>> evaluateDecisionTable(DmnModelInstance modelInstance, String decisionId, ProjectDto project) {
        log.debug("[INIT] evaluateDecisionTable decisionId: {}, project: {}", decisionId, project);
        final List<Map<String, Object>> result = this.evaluateDecisionTable(getDmnEngine(), modelInstance, decisionId, project);
        log.debug("[END] evaluateDecisionTable");
        return result;
    }

    /**
     * Evaluate Project's decision table
     *
     * @param dmnEngine
     * @param modelInstance
     * @param decisionId
     * @param project
     * @return list of project's information that fits decsion table
     */
    private List<Map<String, Object>> evaluateDecisionTable(DmnEngine dmnEngine, DmnModelInstance modelInstance, String decisionId, ProjectDto project) {
        log.debug("[INIT] evaluateDecisionTable decisionId: {}, project: {}", decisionId, project);

        final DmnDecision decision = dmnEngine.parseDecision(decisionId, modelInstance);
        final List<Map<String, Object>> commonEntitiesAccepted = this.evaluateEntities(dmnEngine, decision, project);
        log.debug("[END] evaluateDecisionTable, {} entities has matched filters", commonEntitiesAccepted.size());
        return commonEntitiesAccepted;
    }

    /**
     * Validate DMN file
     *
     * @param dmnFile DMN file to be validated
     * @throws DmnModelException
     */
    @Override
    public final void validateDmn(final byte[] dmnFile) {
        log.debug("[INIT] Validating DMN file");
        DmnEngine dmnEngine = getDmnEngine();
        log.debug("Created default DMN Engine configuration");
        DmnModelInstance dmnModelInstance = Dmn.readModelFromStream(new ByteArrayInputStream(dmnFile));
        log.debug("Read DMN model instance from DMN file");
        List<DmnDecision> decisions = dmnEngine.parseDecisions(dmnModelInstance);
        log.debug("[END] Validated {} decisions found on DMN File", decisions.size());
    }

    /**
     * Execute project.dmnFile
     *
     * @param project that contains DMN file
     * @return entities that fit filters defined on DMN file
     */
    public final List<Map<String, Object>> executeDmn(final ProjectDto project) {
        log.debug("[INIT] executeDmn, Running DMN file {}", Arrays.toString(project.getDmnFile()));

        final DmnModelInstance dmnModelInstance = Dmn.readModelFromStream(new ByteArrayInputStream(project.getDmnFile()));
        List<Map<String, Object>> commonEntitiesAccepted = evaluateDmnDecision(dmnModelInstance, project);

        log.debug("[END] executeDmn, Got {} entities accepted", commonEntitiesAccepted.size());
        return commonEntitiesAccepted;
    }

    /**
     * Add decision tables defined on project.dmnFile
     *
     * @param dmnModelInstance
     * @param project
     * @return list of decision tables
     */
    private List<Map<String, Object>> evaluateDmnDecision(DmnModelInstance dmnModelInstance, ProjectDto project) {
        log.debug("[INIT] evaluateDmnDecision by project: {}", project);
        List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();
        final DmnEngine dmnEngine = getDmnEngine();
        List<DmnDecision> decisions = dmnEngine.parseDecisions(new ByteArrayInputStream(project.getDmnFile()));

        for (DmnDecision decision : decisions) {
            log.info("Evaluating decision {}", decision);
            if (decision.isDecisionTable()) {
                commonEntitiesAccepted.addAll(evaluateDecisionTable(dmnEngine, dmnModelInstance, decision.getKey(), project));
            }
        }
        log.debug("[END] evaluateDmnDecision ");
        return commonEntitiesAccepted;
    }

}
