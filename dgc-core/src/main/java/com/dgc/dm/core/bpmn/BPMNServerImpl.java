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
import org.springframework.mail.MailException;
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
    public void createBPMNModel(final boolean evaluateDecisionTable) {

        final BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("decision-manager")
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
    public List<Map<String, Object>> createBPMNModel(ProjectDto project, List<FilterDto> activeFilters, boolean evaluateDecisionTable, boolean sendMail) throws Exception {

        log.info("Creating DMN model for project " + project);

        return this.generateDmn(project, project.getName() + "decision-manager.dm", "decisionTable-" + project.getName(),
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
    private Definitions createDefinition(DmnModelInstance modelInstance, String definitionName, String definitionId) {
        final Definitions definitions = modelInstance.newInstance(Definitions.class);
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
    private Decision createDecision(DmnModelInstance modelInstance, String decisionId, String decisionName) {
        final Decision decision = modelInstance.newInstance(Decision.class);
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
    private DecisionTable createDecisionTable(DmnModelInstance modelInstance, String decisionTableId, List<FilterDto> activeFilters) {
        final DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
        decisionTable.setId(decisionTableId);
        decisionTable.getInputs().addAll(this.createInputs(modelInstance, activeFilters));
        decisionTable.getOutputs().add(this.createOutput(modelInstance));
        decisionTable.setHitPolicy(HitPolicy.COLLECT);

        return decisionTable;
    }

    private void generateDmnFile(final DmnModelInstance modelInstance, final String outputFilePath) {
        // write the dmn file
        final File dmnFile = new File(outputFilePath);
        Dmn.writeModelToFile(dmnFile, modelInstance);

        log.info("generated dmn file: " + dmnFile.getAbsolutePath());
    }

    private List<Map<String, Object>> generateDmn(ProjectDto project, String outputFilePath, String decisionTableId, String definitionId, String definitionName, String decisionId, String decisionName, List<FilterDto> activeFilters, boolean sendMail, boolean evaluateDecisionTable) throws Exception {
        List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();

        final DmnModelInstance modelInstance = this.createDmnModelInstance(activeFilters, definitionName, definitionId, decisionId, decisionName, decisionTableId, sendMail);

        try {
            log.info("Validating model \n" + IoUtil.convertXmlDocumentToString(modelInstance.getDocument()));
            Dmn.validateModel(modelInstance);

            this.generateDmnFile(modelInstance, outputFilePath);

            if (evaluateDecisionTable) {
                commonEntitiesAccepted = this.evaluateDecisionTable(modelInstance, decisionId, project, sendMail);
            }

            return commonEntitiesAccepted;
        } catch (final ModelValidationException | DmnTransformException e) {
            log.error("Error generating DMN " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error generating DMN " + e.getMessage());
        } catch (final Exception e) {
            log.error("Error " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error " + e.getMessage());
        }

    }

    private DmnModelInstance createDmnModelInstance(List<FilterDto> activeFilters, String definitionName, String definitionId, String decisionId, String decisionName, String decisionTableId, boolean sendMail) {
        final DmnModelInstance modelInstance = Dmn.createEmptyModel();

        // Create definition
        final Definitions definitions = this.createDefinition(modelInstance, definitionName, definitionId);
        modelInstance.setDefinitions(definitions);
        log.debug("Created definition with name" + definitionName + " and id " + definitionId);

        // Create decision
        final Decision decision = this.createDecision(modelInstance, decisionId, decisionName);
        definitions.addChildElement(decision);
        log.debug("Created decision with name" + decisionName + " and id " + decisionId);

        // Create DecisionTable
        final DecisionTable decisionTable = this.createDecisionTable(modelInstance, decisionTableId, activeFilters);
        decision.addChildElement(decisionTable);
        log.debug("Created decisionTable with id " + decisionTableId);

        // Create rule
        final Rule rule = this.createRule(modelInstance, activeFilters, sendMail);
        decisionTable.getRules().add(rule);
        log.debug("Created rule with id " + rule.getId());

        log.debug("ModelInstance successfully created" + modelInstance);
        return modelInstance;
    }

    private Output createOutput(final DmnModelInstance dmnModelInstance) {
        final Output output = dmnModelInstance.newInstance(Output.class);
        output.setId("output1");
        output.setLabel("rule matched?");
        output.setTypeRef("string");
        return output;
    }

    private Collection<? extends Input> createInputs(final DmnModelInstance dmnModelInstance, final List<FilterDto> activeFilters) {
        final List<Input> inputs = new ArrayList<>(activeFilters.size());

        for (int i = 0; i < activeFilters.size(); i++) {
            final FilterDto filter = activeFilters.get(i);
            log.debug("creating input for filter " + filter);
            inputs.add(this.createInput(dmnModelInstance, filter.getName(), filter.getFilterClass()));
        }

        return inputs;
    }

    private Input createInput(final DmnModelInstance dmnModelInstance, final String name, final String filterClass) {

        final Input input = dmnModelInstance.newInstance(Input.class);
        input.addChildElement(this.createInputExpression(dmnModelInstance, name, filterClass));
        return input;
    }

    private ModelElementInstance createInputExpression(final DmnModelInstance dmnModelInstance, final String name, final String filterClass) {
        final InputExpression inputExpression = dmnModelInstance.newInstance(InputExpression.class);
        inputExpression.setTypeRef(filterClass.toLowerCase());
        inputExpression.setId("inputExpression_" + StringUtils.stripAccents(name));

        final Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(name);
        inputExpression.addChildElement(text);

        return inputExpression;
    }

    private Rule createRule(DmnModelInstance dmnModelInstance, List<FilterDto> activeFilters, boolean sendMail) {
        final Rule rule = dmnModelInstance.newInstance(Rule.class);

        for (int i = 0; i < activeFilters.size(); i++) {
            final FilterDto filter = activeFilters.get(i);
            rule.getInputEntries().add(this.createInputEntry(dmnModelInstance, filter.getName(), filter.getValue()));
            log.debug("Added inputEntry by filter" + filter);
        }

        if (sendMail) {
            rule.getOutputEntries().add(this.createOutputEntry(dmnModelInstance, "\"Accepted_sendEMail\"", "Accepted_sendEMail", "Accepted_sendEMail"));
        } else {
            rule.getOutputEntries().add(this.createOutputEntry(dmnModelInstance, "\"Accepted\"", "Accepted", "Accepted"));
        }

        return rule;
    }

    private InputEntry createInputEntry(DmnModelInstance dmnModelInstance, String name, String val) {
        final Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent("\"" + val + "\"");

        final InputEntry inputEntry = dmnModelInstance.newInstance(InputEntry.class);
        inputEntry.setLabel(name);
        inputEntry.setText(text);
        return inputEntry;
    }

    private OutputEntry createOutputEntry(DmnModelInstance dmnModelInstance, String expression, String outputEntryId, String outputEntryLabel) {
        final Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(expression);

        final OutputEntry outputEntry = dmnModelInstance.newInstance(OutputEntry.class);
        outputEntry.setId(outputEntryId);
        outputEntry.setLabel(outputEntryLabel);
        outputEntry.setText(text);
        return outputEntry;
    }


    private VariableMap parseEntityToVariableMap(Map<String, Object> entityMap) {
        final VariableMap variableToBeValidated = Variables.createVariables();

        final Iterator<Map.Entry<String, Object>> iterator = entityMap.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, Object> entity = iterator.next();
            //Skip rowId
            if (!entity.getKey().equals("rowId")) {
                variableToBeValidated.put(entity.getKey(), entity.getValue());
            }
        }
        log.trace("generated variableToBeValidated " + variableToBeValidated);
        return variableToBeValidated;
    }

    private boolean isEntityAccepted(DmnEngine dmnEngine, VariableMap variableToBeValidated, DmnDecision decision) {
        boolean isEntityAccepted = false;

        final DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variableToBeValidated);
        if (result.size() >= 1) {
            isEntityAccepted = true;
        }

        return isEntityAccepted;
    }

    private List<Map<String, Object>> evaluateEntities(DmnEngine dmnEngine, DmnDecision decision, ProjectDto project, boolean sendEmail) {
        final List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();

        final List<Map<String, Object>> commonEntitiesToBeValidated = this.dbServer.getCommonData(project);
        if (commonEntitiesToBeValidated != null && commonEntitiesAccepted.size() > 0) {
            log.warn("Not found entities to be validated");
        } else {
            log.info("Got " + commonEntitiesToBeValidated.size() + " entities to be validated");

            final Filter contactFilter = this.dbServer.getContactFilter(project);

            for (final Map<String, Object> entityMap : commonEntitiesToBeValidated) {
                final VariableMap variableToBeValidated = this.parseEntityToVariableMap(entityMap);

                if (this.isEntityAccepted(dmnEngine, variableToBeValidated, decision)) {
                    commonEntitiesAccepted.add(variableToBeValidated);
                    if (sendEmail) {
                        this.sendEmail(variableToBeValidated, contactFilter, project);
                    }
                }
            }
        }

        return commonEntitiesAccepted;
    }

    private void sendEmail(VariableMap variableToBeValidated, Filter contactFilter, ProjectDto project) {
        if (contactFilter == null) {
            log.warn("sendEmail not found filter having contactFilter active");
        } else {
            final String emailTo = ((variableToBeValidated.get(contactFilter.getName()) == null) ? null : (String) variableToBeValidated.get(contactFilter.getName()));

            if (emailTo != null) {
                try {
                    this.emailService.sendASynchronousMail(emailTo, project);
                } catch (final MailException e) {
                    log.error("Error sending email to " + emailTo + ", error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Map<String, Object>> evaluateDecisionTable(DmnModelInstance modelInstance, String decisionId, ProjectDto project, boolean sendEmail) {

        log.info("Evaluating decision table");
        final DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
        final DmnDecision decision = dmnEngine.parseDecision(decisionId, modelInstance);

        final List<Map<String, Object>> commonEntitiesAccepted = this.evaluateEntities(dmnEngine, decision, project, sendEmail);

        log.info("End validation process, " + commonEntitiesAccepted.size() + " entities has matched filters");
        return commonEntitiesAccepted;
    }


}
