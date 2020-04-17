/*
  @author david
 */

package com.dgc.dm.core.service.bpmn;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.dmn.engine.*;
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

import java.io.*;
import java.util.List;
import java.util.*;

@Slf4j
@Service
public class BPMNServerImpl implements BPMNServer {

    private static final String DEFINITIONS_NAMESPACE = "http://camunda.org/schema/1.0/dmn";

    @Autowired
    private DbServer dbServer;

    @Autowired
    private EmailService emailService;

    /**
     * Create a instance of Definition model
     *
     * @param modelInstance
     * @param definitionName name for new Definition model
     * @param definitionId   id for new Definition model
     * @return instance of Definition model
     */
    private static Definitions createDefinition(final DmnModelInstance modelInstance, final String definitionName, final String definitionId) {
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setNamespace(DEFINITIONS_NAMESPACE);
        definitions.setName(definitionName);
        definitions.setId(definitionId);

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
    private static Decision createDecision(final DmnModelInstance modelInstance, final String decisionId, final String decisionName) {
        Decision decision = modelInstance.newInstance(Decision.class);
        decision.setId(decisionId);
        decision.setName(decisionName);

        return decision;
    }

    private static void generateDmnFile(final ProjectDto project, DmnModelInstance modelInstance, String outputFilePath) {
        // write the dmn file
        File dmnFile = new File(outputFilePath);
        Dmn.writeModelToFile(dmnFile, modelInstance);
        try {
            project.setDmnFile(IOUtils.toByteArray(new FileInputStream(dmnFile)));
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            log.error("Error parsing file to byte  array {}", e.getMessage());
            e.printStackTrace();
        }
        log.info("generated dmn file: {}", dmnFile.getAbsolutePath());
    }

    /**
     * Create a instance of DecisionTable model
     *
     * @param modelInstance
     * @param decisionTableId
     * @param activeFilters
     * @return instance of DecisionTable model
     */
    private DecisionTable createDecisionTable(final DmnModelInstance modelInstance, final String decisionTableId, final List<FilterDto> activeFilters) {
        DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
        decisionTable.setId(decisionTableId);
        decisionTable.getInputs().addAll(createInputs(modelInstance, activeFilters));
        decisionTable.getOutputs().add(createOutput(modelInstance));
        decisionTable.setHitPolicy(HitPolicy.COLLECT);

        return decisionTable;
    }

    private static Output createOutput(DmnModelInstance dmnModelInstance) {
        Output output = dmnModelInstance.newInstance(Output.class);
        output.setId("output1");
        output.setLabel("rule matched?");
        output.setTypeRef("string");
        return output;
    }

    private static ModelElementInstance createInputExpression(DmnModelInstance dmnModelInstance, String name, String filterClass) {
        InputExpression inputExpression = dmnModelInstance.newInstance(InputExpression.class);
        inputExpression.setTypeRef(filterClass.toLowerCase());
        inputExpression.setId("inputExpression_" + StringUtils.stripAccents(name));

        Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(name);
        inputExpression.addChildElement(text);

        return inputExpression;
    }

    private static InputEntry createInputEntry(final DmnModelInstance dmnModelInstance, final String name, final String val) {
        Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent("\"" + val + "\"");

        InputEntry inputEntry = dmnModelInstance.newInstance(InputEntry.class);
        inputEntry.setLabel(name);
        inputEntry.setText(text);
        return inputEntry;
    }

    private static OutputEntry createOutputEntry(final DmnModelInstance dmnModelInstance, final String expression, final String outputEntryId, final String outputEntryLabel) {
        Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(expression);

        OutputEntry outputEntry = dmnModelInstance.newInstance(OutputEntry.class);
        outputEntry.setId(outputEntryId);
        outputEntry.setLabel(outputEntryLabel);
        outputEntry.setText(text);
        return outputEntry;
    }

    private static VariableMap parseEntityToVariableMap(final Map<String, Object> entityMap) {
        VariableMap variableToBeValidated = Variables.createVariables();

        Iterator<Map.Entry<String, Object>> iterator = entityMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entity = iterator.next();
            //Skip rowId
            if (!"rowId".equals(entity.getKey())) {
                variableToBeValidated.put(entity.getKey(), entity.getValue());
            }
        }
        log.trace("generated variableToBeValidated {}", variableToBeValidated);
        return variableToBeValidated;
    }

    private Input createInput(DmnModelInstance dmnModelInstance, String name, String filterClass) {

        Input input = dmnModelInstance.newInstance(Input.class);
        input.addChildElement(createInputExpression(dmnModelInstance, name, filterClass));
        return input;
    }

    private static boolean isEntityAccepted(final DmnEngine dmnEngine, final VariableMap variableToBeValidated, final DmnDecision decision) {
        boolean isEntityAccepted = false;

        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variableToBeValidated);
        if (1 <= result.size()) {
            isEntityAccepted = true;
        }

        return isEntityAccepted;
    }

    private static boolean sendEmail(final DmnDecisionRuleResult firstResult) {
        final boolean sendEmail;
        sendEmail = firstResult.getFirstEntry().equals("\"Accepted_sendEMail\"");
        return sendEmail;
    }

    /**
     * @param project               project created by user
     * @param activeFilters         filters defined by user on decision
     * @param evaluateDecisionTable true if entities must be evaluated using decision table
     * @param sendMail              if true sends email to contact info of Excel's rows that fit activeFilters
     * @return entities that fit filters defined by user
     * @throws Exception
     */
    @Override
    public final List<Map<String, Object>> createBPMNModel(final ProjectDto project, final List<FilterDto> activeFilters, final boolean evaluateDecisionTable, final boolean sendMail) throws Exception {

        log.info("Creating DMN model for project {}", project);

        return generateDmn(project, project.getName() + "decision-manager.dm", "decisionTable-" + project.getName(),
                "definition-" + project.getId(), "definition-" + project.getName(), "decision-" + project.getId(),
                "decision-" + project.getName(), activeFilters, sendMail, evaluateDecisionTable);
    }

    private List<Map<String, Object>> generateDmn(final ProjectDto project, final String outputFilePath, final String decisionTableId, final String definitionId, final String definitionName, final String decisionId, final String decisionName, final List<FilterDto> activeFilters, final boolean sendMail, final boolean evaluateDecisionTable) throws Exception {
        List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();

        DmnModelInstance modelInstance = createDmnModelInstance(activeFilters, definitionName, definitionId, decisionId, decisionName, decisionTableId, sendMail);

        try {
            log.info("Validating model \n{}", IoUtil.convertXmlDocumentToString(modelInstance.getDocument()));
            Dmn.validateModel(modelInstance);

            generateDmnFile(project, modelInstance, outputFilePath);
            //Add dmn file to project
            this.dbServer.updateProject(project);

            if (evaluateDecisionTable) {
                final DmnEngine dmnEngine = DmnEngineConfiguration
                        .createDefaultDmnEngineConfiguration()
                        .buildEngine();
                commonEntitiesAccepted = evaluateDecisionTable(dmnEngine, modelInstance, decisionId, project);
            }

            return commonEntitiesAccepted;
        } catch (ModelValidationException | DmnTransformException e) {
            log.error("Error generating DMN {}", e.getMessage());
            e.printStackTrace();
            throw new Exception("Error generating DMN " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error {}", e.getMessage());
            e.printStackTrace();
            throw new Exception("Error " + e.getMessage());
        }

    }

    private DmnModelInstance createDmnModelInstance(final List<FilterDto> activeFilters, final String definitionName, final String definitionId, final String decisionId, final String decisionName, final String decisionTableId, final boolean sendMail) {
        DmnModelInstance modelInstance = Dmn.createEmptyModel();

        // Create definition
        Definitions definitions = createDefinition(modelInstance, definitionName, definitionId);
        modelInstance.setDefinitions(definitions);
        log.debug("Created definition with name{} and id {}", definitionName, definitionId);

        // Create decision
        Decision decision = createDecision(modelInstance, decisionId, decisionName);
        definitions.addChildElement(decision);
        log.debug("Created decision with name{} and id {}", decisionName, decisionId);

        // Create DecisionTable
        DecisionTable decisionTable = createDecisionTable(modelInstance, decisionTableId, activeFilters);
        decision.addChildElement(decisionTable);
        log.debug("Created decisionTable with id {}", decisionTableId);

        // Create rule
        Rule rule = createRule(modelInstance, activeFilters, sendMail);
        decisionTable.getRules().add(rule);
        log.debug("Created rule with id {}", rule.getId());

        log.debug("ModelInstance successfully created{}", modelInstance);
        return modelInstance;
    }

    private Collection<? extends Input> createInputs(DmnModelInstance dmnModelInstance, List<FilterDto> activeFilters) {
        List<Input> inputs = new ArrayList<>(activeFilters.size());

        for (FilterDto filter : activeFilters) {
            log.debug("creating input for filter {}", filter);
            inputs.add(createInput(dmnModelInstance, filter.getName(), filter.getFilterClass()));
        }

        return inputs;
    }

    private Rule createRule(final DmnModelInstance dmnModelInstance, final List<FilterDto> activeFilters, final boolean sendMail) {
        Rule rule = dmnModelInstance.newInstance(Rule.class);

        for (FilterDto filter : activeFilters) {
            rule.getInputEntries().add(createInputEntry(dmnModelInstance, filter.getName(), filter.getValue()));
            log.debug("Added inputEntry by filter{}", filter);
        }

        if (sendMail) {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"Accepted_sendEMail\"", "Accepted_sendEMail", "Accepted_sendEMail"));
        } else {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"Accepted\"", "Accepted", "Accepted"));
        }

        return rule;
    }

    private List<Map<String, Object>> evaluateEntities(final DmnEngine dmnEngine, final DmnDecision decision, final ProjectDto project) {
        List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();

        List<Map<String, Object>> commonEntitiesToBeValidated = dbServer.getCommonData(project);
        log.info("Got {} entities to be validated", commonEntitiesToBeValidated.size());

        Filter contactFilter = dbServer.getContactFilter(project);

        for (Map<String, Object> entityMap : commonEntitiesToBeValidated) {
            VariableMap variableToBeValidated = parseEntityToVariableMap(entityMap);

            DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variableToBeValidated);

            if (1 <= result.size()) {
                commonEntitiesAccepted.add(variableToBeValidated);
                if (sendEmail(result.getFirstResult())) {
                    sendEmail(variableToBeValidated, contactFilter, project);
                }
            }
        }

        return commonEntitiesAccepted;
    }

    private void sendEmail(final VariableMap variableToBeValidated, final Filter contactFilter, final ProjectDto project) {
        if (null == contactFilter) {
            log.warn("sendEmail not found filter having contactFilter active");
        } else {
            String emailTo = ((null == variableToBeValidated.get(contactFilter.getName())) ? null : (String) variableToBeValidated.get(contactFilter.getName()));

            if (null != emailTo) {
                try {
                    emailService.sendASynchronousMail(emailTo, project);
                } catch (MailException e) {
                    log.error("Error sending email to {}, error: {}", emailTo, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Map<String, Object>> evaluateDecisionTable(final DmnEngine dmnEngine, final DmnModelInstance modelInstance, final String decisionId, final ProjectDto project) {

        log.info("Evaluating decision table");
        DmnDecision decision = dmnEngine.parseDecision(decisionId, modelInstance);

        List<Map<String, Object>> commonEntitiesAccepted = evaluateEntities(dmnEngine, decision, project);

        log.info("End validation process, {} entities has matched filters", commonEntitiesAccepted.size());
        return commonEntitiesAccepted;
    }

    /**
     * Validate DMN file
     *
     * @param dmnFile DMN file to be validated
     * @throws DmnModelException
     */
    @Override
    public final void validateDmn(byte[] dmnFile) {
        log.info("Validating DMN file");

        final DmnEngine dmnEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();
        log.debug("Created default DMN Engine configuration");

        final DmnModelInstance dmnModelInstance = Dmn.readModelFromStream(new ByteArrayInputStream(dmnFile));
        log.debug("Read DMN model instance from DMN file");

        final List<DmnDecision> decisions = dmnEngine.parseDecisions(dmnModelInstance);

        log.info("Validated decisions found on DMN File");
    }

    /**
     * @param project that contains DMN file
     * @return entities that fit filters defined on DMN file
     */
    public final List<Map<String, Object>> executeDmn(ProjectDto project) {
        log.info("Running DMN file {}", Arrays.toString(project.getDmnFile()));

        DmnModelInstance dmnModelInstance = Dmn.readModelFromStream(new ByteArrayInputStream(project.getDmnFile()));

        final List<Map<String, Object>> commonEntitiesAccepted = this.evaluateDmnDecision(dmnModelInstance, project);

        log.info("Got {} entities accepted", commonEntitiesAccepted.size());

        return commonEntitiesAccepted;
    }

    private List<Map<String, Object>> evaluateDmnDecision(final DmnModelInstance dmnModelInstance, final ProjectDto project) {
        final List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();

        DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
        final List<DmnDecision> decisions = dmnEngine.parseDecisions(new ByteArrayInputStream(project.getDmnFile()));

        for (final DmnDecision decision : decisions) {
            log.info("Evaluating decision {}", decision);
            if (decision.isDecisionTable()) {

                commonEntitiesAccepted.addAll(this.evaluateDecisionTable(dmnEngine, dmnModelInstance, decision.getKey(), project));
            }
        }

        return commonEntitiesAccepted;
    }
}
