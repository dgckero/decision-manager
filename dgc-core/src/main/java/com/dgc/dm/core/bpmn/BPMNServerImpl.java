/*
  @author david
 */

package com.dgc.dm.core.bpmn;

import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.FilterDto;
import lombok.extern.slf4j.Slf4j;
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

    @Autowired
    private DbServer dbServer;

    @Override
    public void createBPMNModel() {

        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("decision-manager")
                .name("BPMN API Invoice Process")
                .done();

    }

    @Override
    public void createBPMNModel(List<FilterDto> activeFilters) throws Exception {

        BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("decision-manager")
                .name("BPMN API Invoice Process")
                .done();
        boolean sendMail = false;

        generateDmn("decision-manager.dm", "dm-decisionTable", "dm-definitionId", "dm-definitionName", "dm-decisionId", "dm-decisionName", activeFilters, sendMail);
    }


    public void generateDmn(String output, String decisionTableId, String definitionId, String definitionName, String decisionId, String decisionName, List<FilterDto> activeFilters, boolean sendMail) throws Exception {
        DmnModelInstance modelInstance = Dmn.createEmptyModel();

        // Create definition
        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setNamespace("http://camunda.org/schema/1.0/dmn");
        definitions.setName(definitionName);
        definitions.setId(definitionId);
        modelInstance.setDefinitions(definitions);

        // Create decision
        Decision decision = modelInstance.newInstance(Decision.class);
        decision.setId(decisionId);
        decision.setName(decisionName);
        definitions.addChildElement(decision);

        // Create DecisionTable
        DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
        decisionTable.setId(decisionTableId);
        decisionTable.getInputs().addAll(createInputs(modelInstance, activeFilters));
        decisionTable.getOutputs().add(createOutput(modelInstance));
        decisionTable.setHitPolicy(HitPolicy.COLLECT);
        decision.addChildElement(decisionTable);

        // Create rule
        Rule rule = createRule(modelInstance, activeFilters, sendMail);
        decisionTable.getRules().add(rule);

        try {
            log.info("modelString before to be validated\n" + IoUtil.convertXmlDocumentToString(modelInstance.getDocument()));

            Dmn.validateModel(modelInstance);

            // write the dmn file
            File dmnFile = new File(output);
            Dmn.writeModelToFile(dmnFile, modelInstance);

            log.info("generate dmn file: " + dmnFile.getAbsolutePath());

            evaluateDecisionTable(modelInstance, decisionId);

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
        inputExpression.setId("inputExpression_" + name);

        Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent("\"" + name + "\"");
        inputExpression.addChildElement(text);

        return inputExpression;
    }

    private Rule createRule(DmnModelInstance dmnModelInstance, List<FilterDto> activeFilters, boolean sendMail) {

        Rule rule = dmnModelInstance.newInstance(Rule.class);

        for (int i = 0; i < activeFilters.size(); i++) {
            FilterDto filter = activeFilters.get(i);
            log.debug("processing filter " + filter);
//            InputEntry inputEntryFilterName = createInputEntry(dmnModelInstance, filter.getName());
//            rule.getInputEntries().add(inputEntryFilterName);
            InputEntry inputEntryFilterVal = createInputEntry(dmnModelInstance, filter.getValue());
            rule.getInputEntries().add(inputEntryFilterVal);
        }

        if (sendMail) {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"Accepted_sendEMail\""));
        } else {
            rule.getOutputEntries().add(createOutputEntry(dmnModelInstance, "\"Accepted\""));
        }

        return rule;
    }

    private InputEntry createInputEntry(DmnModelInstance dmnModelInstance, String expression) {
        Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(expression);

        InputEntry inputEntry = dmnModelInstance.newInstance(InputEntry.class);
        inputEntry.setText(text);
        return inputEntry;
    }

    private OutputEntry createOutputEntry(DmnModelInstance dmnModelInstance, String expression) {
        Text text = dmnModelInstance.newInstance(Text.class);
        text.setTextContent(expression);

        OutputEntry outputEntry = dmnModelInstance.newInstance(OutputEntry.class);
        outputEntry.setText(text);
        return outputEntry;
    }

    private void evaluateDecisionTable(DmnModelInstance modelInstance, String decisionId) {

        DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
        DmnDecision decision = dmnEngine.parseDecision(decisionId, modelInstance);

        List<Map<String, Object>> commonEntities = dbServer.getCommonData();
//TODO la tabla de decisión es para evaluar registro a registro, igual esto vale mejor para ver el tema del email, pero nada más
        VariableMap variables = Variables.createVariables();
        for (Map<String, Object> entityMap : commonEntities) {
            Iterator<Map.Entry<String, Object>> iterator = entityMap.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<String, Object> entity = iterator.next();
                variables.put(entity.getKey(), entity.getValue());
            }
        }

        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

        System.out.println(result.toString());

        Collection<Input> inputs = modelInstance.getModelElementsByType(Input.class);
        for (Input input2 : inputs) {
            System.out.println("" + input2.getRawTextContent());
        }
        System.out.println();
        Collection<InputEntry> inputEntries = modelInstance.getModelElementsByType(InputEntry.class);
        for (InputEntry inputEntry3 : inputEntries) {
            System.out.println("" + inputEntry3.getRawTextContent());

        }

    }

}
