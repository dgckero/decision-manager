/*
  @author david
 */

package com.dgc.dm.core.service.bpmn;

import com.dgc.dm.core.dto.FilterDto;
import com.dgc.dm.core.dto.ProjectDto;
import com.dgc.dm.core.service.db.DataService;
import com.dgc.dm.core.service.db.FilterService;
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
    private static final String ACCEPTED_AND_SEND_EMAIL = "Accepted_sendEMail";
    private static final String ACCEPTED = "Accepted";

    @Autowired
    private EmailService emailService;
    @Autowired
    private DataService dataService;
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
    private static Decision createDecision(DmnModelInstance modelInstance, String decisionId, String decisionName) {
        final Decision decision = modelInstance.newInstance(Decision.class);
        decision.setId(decisionId);
        decision.setName(decisionName);

        return decision;
    }

    private static void generateDmnFile(ProjectDto project, final DmnModelInstance modelInstance, final String outputFilePath) {
        // write the dmn file
        final File dmnFile = new File(outputFilePath);
        Dmn.writeModelToFile(dmnFile, modelInstance);
        try {
            project.setDmnFile(IOUtils.toByteArray(new FileInputStream(dmnFile)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Error parsing file to byte  array {}", e.getMessage());
            e.printStackTrace();
        }
        log.info("generated dmn file: {}", dmnFile.getAbsolutePath());
    }

    private static Output createOutput(final DmnModelInstance dmnModelInstance) {
        final Output output = dmnModelInstance.newInstance(Output.class);
        output.setId("output1");
        output.setLabel("rule matched?");
        output.setTypeRef("string");
        return output;
    }

    private static ModelElementInstance createInputExpression(final DmnModelInstance dmnModelInstance, final String name, final String filterClass) {
        final InputExpression inputExpression = dmnModelInstance.newInstance(InputExpression.class);
        inputExpression.setTypeRef(filterClass.toLowerCase());
        inputExpression.setId("inputExpression_" + StringUtils.stripAccents(name));

        final Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(name);
        inputExpression.addChildElement(text);

        return inputExpression;
    }

    private static InputEntry createInputEntry(DmnModelInstance dmnModelInstance, String name, String val) {
        final Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent("\"" + val + "\"");

        final InputEntry inputEntry = dmnModelInstance.newInstance(InputEntry.class);
        inputEntry.setLabel(name);
        inputEntry.setText(text);
        return inputEntry;
    }

    private static OutputEntry createOutputEntry(DmnModelInstance dmnModelInstance, String expression, String outputEntryId, String outputEntryLabel) {
        final Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(expression);

        final OutputEntry outputEntry = dmnModelInstance.newInstance(OutputEntry.class);
        outputEntry.setId(outputEntryId);
        outputEntry.setLabel(outputEntryLabel);
        outputEntry.setText(text);
        return outputEntry;
    }

    private static VariableMap parseEntityToVariableMap(Map<String, Object> entityMap) {
        final VariableMap variableToBeValidated = Variables.createVariables();

        final Iterator<Map.Entry<String, Object>> iterator = entityMap.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, Object> entity = iterator.next();
            //Skip rowId
            if (!"rowId".equals(entity.getKey())) {
                variableToBeValidated.put(entity.getKey(), entity.getValue());
            }
        }
        log.trace("generated variableToBeValidated {}", variableToBeValidated);
        return variableToBeValidated;
    }

    private static boolean isEntityAccepted(DmnEngine dmnEngine, VariableMap variableToBeValidated, DmnDecision decision) {
        boolean isEntityAccepted = false;

        final DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variableToBeValidated);
        if (1 <= result.size()) {
            isEntityAccepted = true;
        }

        return isEntityAccepted;
    }

    private static boolean sendEmail(DmnDecisionRuleResult firstResult) {
        boolean sendEmail;
        sendEmail = Objects.equals(firstResult.getFirstEntry(), ACCEPTED_AND_SEND_EMAIL);
        return sendEmail;
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
        decisionTable.getOutputs().add(createOutput(modelInstance));
        decisionTable.setHitPolicy(HitPolicy.COLLECT);

        return decisionTable;
    }

    private Input createInput(final DmnModelInstance dmnModelInstance, final String name, final String filterClass) {

        final Input input = dmnModelInstance.newInstance(Input.class);
        input.addChildElement(createInputExpression(dmnModelInstance, name, filterClass));
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

        log.info("Creating DMN model for project {}", project);

        return this.generateDmn(project, project.getName() + "decision-manager.dm", "decisionTable-" + project.getName(),
                "definition-" + project.getId(), "definition-" + project.getName(), "decision-" + project.getId(),
                "decision-" + project.getName(), activeFilters, sendMail);
    }

    private List<Map<String, Object>> generateDmn(ProjectDto project, String outputFilePath, String decisionTableId, String definitionId, String definitionName, String decisionId, String decisionName, List<FilterDto> activeFilters, Boolean sendMail) throws Exception {
        List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();
        try {
            final DmnModelInstance modelInstance = this.createDmnModelInstance(activeFilters, definitionName, definitionId, decisionId, decisionName, decisionTableId, sendMail);

            log.info("Validating model \n{}", IoUtil.convertXmlDocumentToString(modelInstance.getDocument()));
            Dmn.validateModel(modelInstance);

            generateDmnFile(project, modelInstance, outputFilePath);

            commonEntitiesAccepted = this.evaluateDecisionTable(modelInstance, decisionId, project);

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

    private DmnModelInstance createDmnModelInstance(List<FilterDto> activeFilters, String definitionName, String definitionId, String decisionId, String decisionName, String decisionTableId, Boolean sendMail) {
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

        log.debug("ModelInstance successfully created{}", modelInstance);
        return modelInstance;
    }

    private Collection<? extends Input> createInputs(final DmnModelInstance dmnModelInstance, final List<FilterDto> activeFilters) {
        final List<Input> inputs = new ArrayList<>(activeFilters.size());

        for (final FilterDto filter : activeFilters) {
            log.debug("creating input for filter {}", filter);
            inputs.add(this.createInput(dmnModelInstance, filter.getName(), filter.getFilterClass()));
        }

        return inputs;
    }

    private Rule createRule(DmnModelInstance dmnModelInstance, List<FilterDto> activeFilters, Boolean sendMail) {
        final Rule rule = dmnModelInstance.newInstance(Rule.class);

        for (final FilterDto filter : activeFilters) {
            rule.getInputEntries().add(createInputEntry(dmnModelInstance, filter.getName(), filter.getValue()));
            log.debug("Added inputEntry by filter{}", filter);
        }

        if (null == sendMail || !sendMail) {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"" + ACCEPTED + "\"", ACCEPTED, ACCEPTED));
        } else {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"" + ACCEPTED_AND_SEND_EMAIL + "\"", ACCEPTED_AND_SEND_EMAIL, ACCEPTED_AND_SEND_EMAIL));
        }

        return rule;
    }

    private List<Map<String, Object>> evaluateEntities(DmnEngine dmnEngine, DmnDecision decision, ProjectDto project) {
        final List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();

        final List<Map<String, Object>> commonEntitiesToBeValidated = this.dataService.getCommonData(project);
        log.info("Got {} entities to be validated", commonEntitiesToBeValidated.size());

        final FilterDto contactFilter = this.filterService.getContactFilter(project);

        for (final Map<String, Object> entityMap : commonEntitiesToBeValidated) {
            final VariableMap variableToBeValidated = parseEntityToVariableMap(entityMap);

            final DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variableToBeValidated);

            if (1 <= result.size()) {
                commonEntitiesAccepted.add(variableToBeValidated);
                if (sendEmail(result.getFirstResult())) {
                    log.debug("Result found with email information");
                    this.sendEmail(variableToBeValidated, contactFilter, project);
                }
            }
        }

        return commonEntitiesAccepted;
    }

    private void sendEmail(VariableMap variableToBeValidated, FilterDto contactFilter, ProjectDto project) {
        if (null == contactFilter) {
            log.warn("sendEmail not found filter having contactFilter active");
        } else {
            final String emailTo = ((null == variableToBeValidated.get(contactFilter.getName())) ? null : (String) variableToBeValidated.get(contactFilter.getName()));

            if (null != emailTo) {
                try {
                    log.trace("Sending email to {}", emailTo);
                    this.emailService.sendASynchronousMail(emailTo, project);
                } catch (final MailException e) {
                    log.error("Error sending email to {}, error: {}", emailTo, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private List<Map<String, Object>> evaluateDecisionTable(DmnModelInstance modelInstance, String decisionId, ProjectDto project) {

        log.info("Evaluating decision table");
        DmnEngine dmnEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();

        return this.evaluateDecisionTable(dmnEngine, modelInstance, decisionId, project);
    }

    private List<Map<String, Object>> evaluateDecisionTable(DmnEngine dmnEngine, DmnModelInstance modelInstance, String decisionId, ProjectDto project) {

        log.info("Evaluating decision table");

        final DmnDecision decision = dmnEngine.parseDecision(decisionId, modelInstance);

        final List<Map<String, Object>> commonEntitiesAccepted = this.evaluateEntities(dmnEngine, decision, project);

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
    public final void validateDmn(final byte[] dmnFile) {
        log.info("Validating DMN file");

        DmnEngine dmnEngine = DmnEngineConfiguration
                .createDefaultDmnEngineConfiguration()
                .buildEngine();
        log.debug("Created default DMN Engine configuration");

        DmnModelInstance dmnModelInstance = Dmn.readModelFromStream(new ByteArrayInputStream(dmnFile));
        log.debug("Read DMN model instance from DMN file");

        List<DmnDecision> decisions = dmnEngine.parseDecisions(dmnModelInstance);

        log.info("Validated {} decisions found on DMN File", decisions.size());
    }

    /**
     * @param project that contains DMN file
     * @return entities that fit filters defined on DMN file
     */
    public final List<Map<String, Object>> executeDmn(final ProjectDto project) {
        log.info("Running DMN file {}", Arrays.toString(project.getDmnFile()));

        final DmnModelInstance dmnModelInstance = Dmn.readModelFromStream(new ByteArrayInputStream(project.getDmnFile()));

        List<Map<String, Object>> commonEntitiesAccepted = evaluateDmnDecision(dmnModelInstance, project);

        log.info("Got {} entities accepted", commonEntitiesAccepted.size());

        return commonEntitiesAccepted;
    }

    private List<Map<String, Object>> evaluateDmnDecision(DmnModelInstance dmnModelInstance, ProjectDto project) {
        List<Map<String, Object>> commonEntitiesAccepted = new ArrayList<>();

        final DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
        List<DmnDecision> decisions = dmnEngine.parseDecisions(new ByteArrayInputStream(project.getDmnFile()));

        for (DmnDecision decision : decisions) {
            log.info("Evaluating decision {}", decision);
            if (decision.isDecisionTable()) {
                commonEntitiesAccepted.addAll(evaluateDecisionTable(dmnEngine, dmnModelInstance, decision.getKey(), project));
            }
        }

        return commonEntitiesAccepted;
    }

}
