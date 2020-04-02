/*
  @author david
 */

package com.dgc.dm.core.bpmn;

import com.dgc.dm.core.db.model.Filter;
import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformException;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.*;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.camunda.bpm.model.xml.impl.util.IoUtil;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
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
     * create BpmnModelInstance
     *
     * @param evaluateDecisionTable
     */
    @Override
    public void createBPMNModel(boolean evaluateDecisionTable) {

        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("decision-manager")
                .name("BPMN API Invoice Process")
                .done();

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
    public List<Map<String, Object>> createBPMNModel(final ProjectDto project, final List<FilterDto> activeFilters, final boolean evaluateDecisionTable, final boolean sendMail) throws Exception {

        log.info("Creating DMN model for project " + project);

        return generateDmn(project, project.getName() + "decision-manager.dm", "decisionTable-" + project.getName(),
                "definition-" + project.getId(), "definition-" + project.getName(), "decision-" + project.getId(),
                "decision-" + project.getName(), activeFilters, sendMail, evaluateDecisionTable);
    }

    /**
     * Create a instance of Definition model
     *
     * @param modelInstance
     * @param definitionName name for new Definition model
     * @param definitionId   id for new Definition model
     * @return instance of Definition model
     */
    private Definitions createDefinition(final DmnModelInstance modelInstance, final String definitionName, final String definitionId) {
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
    private Decision createDecision(final DmnModelInstance modelInstance, final String decisionId, final String decisionName) {
        Decision decision = modelInstance.newInstance(Decision.class);
        decision.setId(decisionId);
        decision.setName(decisionName);

        return decision;
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

    private void generateDmnFile(DmnModelInstance modelInstance, String outputFilePath) {
        // write the dmn file
        File dmnFile = new File(outputFilePath);
        Dmn.writeModelToFile(dmnFile, modelInstance);

        log.info("generated dmn file: " + dmnFile.getAbsolutePath());
    }

    private List<Map<String, Object>> generateDmn(final ProjectDto project, final String outputFilePath, final String decisionTableId, final String definitionId, final String definitionName, final String decisionId, final String decisionName, final List<FilterDto> activeFilters, final boolean sendMail, final boolean evaluateDecisionTable) throws Exception {
        List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();

        DmnModelInstance modelInstance = createDmnModelInstance(activeFilters, definitionName, definitionId, decisionId, decisionName, decisionTableId, sendMail);

        try {
            log.info("Validating model \n" + IoUtil.convertXmlDocumentToString(modelInstance.getDocument()));
            Dmn.validateModel(modelInstance);

            generateDmnFile(modelInstance, outputFilePath);

            if (evaluateDecisionTable) {
                commonEntitiesAccepted = evaluateDecisionTable(modelInstance, decisionId, project, sendMail);
            }

            return commonEntitiesAccepted;
        } catch (ModelValidationException | DmnTransformException e) {
            log.error("Error generating DMN " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error generating DMN " + e.getMessage());
        } catch (Exception e) {
            log.error("Error " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error " + e.getMessage());
        }

    }

    private DmnModelInstance createDmnModelInstance(final List<FilterDto> activeFilters, final String definitionName, final String definitionId, final String decisionId, final String decisionName, final String decisionTableId, final boolean sendMail) {
        DmnModelInstance modelInstance = Dmn.createEmptyModel();

        // Create definition
        Definitions definitions = createDefinition(modelInstance, definitionName, definitionId);
        modelInstance.setDefinitions(definitions);
        log.debug("Created definition with name" + definitionName + " and id " + definitionId);

        // Create decision
        Decision decision = createDecision(modelInstance, decisionId, decisionName);
        definitions.addChildElement(decision);
        log.debug("Created decision with name" + decisionName + " and id " + decisionId);

        // Create DecisionTable
        DecisionTable decisionTable = createDecisionTable(modelInstance, decisionTableId, activeFilters);
        decision.addChildElement(decisionTable);
        log.debug("Created decisionTable with id " + decisionTableId);

        // Create rule
        Rule rule = createRule(modelInstance, activeFilters, sendMail);
        decisionTable.getRules().add(rule);
        log.debug("Created rule with id " + rule.getId());

        log.debug("ModelInstance successfully created" + modelInstance);
        return modelInstance;
    }

    private Output createOutput(DmnModelInstance dmnModelInstance) {
        Output output = dmnModelInstance.newInstance(Output.class);
        output.setId("output1");
        output.setLabel("rule matched?");
        output.setTypeRef("string");
        return output;
    }

    private Collection<? extends Input> createInputs(DmnModelInstance dmnModelInstance, List<FilterDto> activeFilters) {
        List<Input> inputs = new ArrayList<>(activeFilters.size());

        for (int i = 0; i < activeFilters.size(); i++) {
            FilterDto filter = activeFilters.get(i);
            log.debug("creating input for filter " + filter);
            inputs.add(createInput(dmnModelInstance, filter.getName(), filter.getFilterClass()));
        }

        return inputs;
    }

    private Input createInput(DmnModelInstance dmnModelInstance, String name, String filterClass) {

        Input input = dmnModelInstance.newInstance(Input.class);
        input.addChildElement(createInputExpression(dmnModelInstance, name, filterClass));
        return input;
    }

    private ModelElementInstance createInputExpression(DmnModelInstance dmnModelInstance, String name, String filterClass) {
        InputExpression inputExpression = dmnModelInstance.newInstance(InputExpression.class);
        inputExpression.setTypeRef(filterClass.toLowerCase());
        inputExpression.setId("inputExpression_" + StringUtils.stripAccents(name));

        Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(name);
        inputExpression.addChildElement(text);

        return inputExpression;
    }

    private Rule createRule(final DmnModelInstance dmnModelInstance, final List<FilterDto> activeFilters, final boolean sendMail) {
        Rule rule = dmnModelInstance.newInstance(Rule.class);

        for (int i = 0; i < activeFilters.size(); i++) {
            FilterDto filter = activeFilters.get(i);
            rule.getInputEntries().add(createInputEntry(dmnModelInstance, filter.getName(), filter.getValue()));
            log.debug("Added inputEntry by filter" + filter);
        }

        if (sendMail) {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"Accepted_sendEMail\"", "Accepted_sendEMail", "Accepted_sendEMail"));
        } else {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"Accepted\"", "Accepted", "Accepted"));
        }

        return rule;
    }

    private InputEntry createInputEntry(final DmnModelInstance dmnModelInstance, final String name, final String val) {
        Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent("\"" + val + "\"");

        InputEntry inputEntry = dmnModelInstance.newInstance(InputEntry.class);
        inputEntry.setLabel(name);
        inputEntry.setText(text);
        return inputEntry;
    }

    private OutputEntry createOutputEntry(final DmnModelInstance dmnModelInstance, final String expression, final String outputEntryId, final String outputEntryLabel) {
        Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(expression);

        OutputEntry outputEntry = dmnModelInstance.newInstance(OutputEntry.class);
        outputEntry.setId(outputEntryId);
        outputEntry.setLabel(outputEntryLabel);
        outputEntry.setText(text);
        return outputEntry;
    }


    private VariableMap parseEntityToVariableMap(final Map<String, Object> entityMap) {
        VariableMap variableToBeValidated = Variables.createVariables();

        Iterator<Map.Entry<String, Object>> iterator = entityMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> entity = iterator.next();
            //Skip rowId
            if (!entity.getKey().equals("rowId")) {
                variableToBeValidated.put(entity.getKey(), entity.getValue());
            }
        }
        log.trace("generated variableToBeValidated " + variableToBeValidated);
        return variableToBeValidated;
    }

    private boolean isEntityAccepted(final DmnEngine dmnEngine, final VariableMap variableToBeValidated, final DmnDecision decision) {
        boolean isEntityAccepted = false;

        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variableToBeValidated);
        if (result.size() >= 1) {
            isEntityAccepted = true;
        }

        return isEntityAccepted;
    }

    private List<Map<String, Object>> evaluateEntities(final DmnEngine dmnEngine, final DmnDecision decision, final ProjectDto project, final boolean sendEmail) {
        List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();

        List<Map<String, Object>> commonEntitiesToBeValidated = dbServer.getCommonData(project);
        if (commonEntitiesToBeValidated != null && commonEntitiesAccepted.size() > 0) {
            log.warn("Not found entities to be validated");
        } else {
            log.info("Got " + commonEntitiesToBeValidated.size() + " entities to be validated");

            Filter contactFilter = dbServer.getContactFilter(project);

            for (Map<String, Object> entityMap : commonEntitiesToBeValidated) {
                VariableMap variableToBeValidated = parseEntityToVariableMap(entityMap);

                if (isEntityAccepted(dmnEngine, variableToBeValidated, decision)) {
                    commonEntitiesAccepted.add(variableToBeValidated);
                    if (sendEmail) {
                        sendEmail(variableToBeValidated, contactFilter, project);
                    }
                }
            }
        }

        return commonEntitiesAccepted;
    }

    private void sendEmail(final VariableMap variableToBeValidated, final Filter contactFilter, final ProjectDto project) {
        if (contactFilter == null) {
            log.warn("sendEmail not found filter having contactFilter active");
        } else {
            String emailTo = ((variableToBeValidated.get(contactFilter.getName()) == null) ? null : (String) variableToBeValidated.get(contactFilter.getName()));

            if (emailTo != null) {
                try {
                    emailService.sendASynchronousMail(emailTo, project);
                } catch (RuntimeException e) {
                    log.error("Error sending email to " + emailTo + ", error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Map<String, Object>> evaluateDecisionTable(final DmnModelInstance modelInstance, final String decisionId, final ProjectDto project, final boolean sendEmail) {

        log.info("Evaluating decision table");
        DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
        DmnDecision decision = dmnEngine.parseDecision(decisionId, modelInstance);

        List<Map<String, Object>> commonEntitiesAccepted = evaluateEntities(dmnEngine, decision, project, sendEmail);

        log.info("End validation process, " + commonEntitiesAccepted.size() + " entities has matched filters");
        return commonEntitiesAccepted;
    }


}
