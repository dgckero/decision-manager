/*
  @author david
 */

package com.dgc.dm.core.bpmn;

import com.dgc.dm.core.db.model.CommonEntity;
import com.dgc.dm.core.db.service.DbServer;
import com.dgc.dm.core.dto.FilterDto;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.instance.*;
import org.camunda.bpm.model.xml.ModelValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

        createDMNEngine(activeFilters, true);
    }

    private void createDMNEngine(List<FilterDto> activeFilters, boolean sendMail) throws Exception {

        DmnModelInstance modelInstance = Dmn.createEmptyModel();

        Definitions definitions = modelInstance.newInstance(Definitions.class);
        definitions.setNamespace("http://camunda.org/schema/1.0/dmn");
        definitions.setName("definitions");
        definitions.setId("definitions");
        modelInstance.setDefinitions(definitions);

        Decision decision = modelInstance.newInstance(Decision.class);
        decision.setId("decisionGenerated");
        decision.setName("generationDecision");
        definitions.addChildElement(decision);

        DecisionTable decisionTable = modelInstance.newInstance(DecisionTable.class);
        decisionTable.setId("decisionTable");
        decisionTable.setHitPolicy(HitPolicy.UNIQUE);
        decision.addChildElement(decisionTable);


        for (FilterDto filter : activeFilters) {

            Input filterInput = modelInstance.newInstance(Input.class);
            filterInput.setId("Input_" + filter.getId());
            filterInput.setLabel(filter.getName());

            InputExpression inputExpression = modelInstance.newInstance(InputExpression.class);
            inputExpression.setId("InputExpression_" + filter.getId());
            inputExpression.setTypeRef(filter.getFilterClass());
            Text text = modelInstance.newInstance(Text.class);
            text.setTextContent(filter.getName());
            inputExpression.setText(text);
            filterInput.addChildElement(inputExpression);
            decisionTable.addChildElement(filterInput);

            // Regla
            Rule rule = modelInstance.newInstance(Rule.class);
            rule.setId("Rule_" + filter.getId());
            Text text1 = modelInstance.newInstance(Text.class);
            text1.setTextContent(filter.getValue());
            InputEntry inputEntry = modelInstance.newInstance(InputEntry.class);
            inputEntry.setId(String.valueOf(filter.getId()));
            inputEntry.addChildElement(text1);
            rule.addChildElement(inputEntry);


            OutputEntry acceptedOutputEntry = modelInstance.newInstance(OutputEntry.class);
            acceptedOutputEntry.setId("output_acepted");
            acceptedOutputEntry.setLabel("Accepted");
            Text acceptedOutputText = modelInstance.newInstance(Text.class);
            acceptedOutputText.setTextContent("True");
            acceptedOutputEntry.addChildElement(acceptedOutputText);
            acceptedOutputEntry.setTextContent("\"Accepted\"");

            rule.addChildElement(acceptedOutputEntry);

            if (sendMail) {
                OutputEntry emailOutputEntry = modelInstance.newInstance(OutputEntry.class);
                emailOutputEntry.setId("output_sendEmail");
                emailOutputEntry.setLabel("sendEmail");
                Text sendEmailOutput = modelInstance.newInstance(Text.class);
                sendEmailOutput.setTextContent("True");
                sendEmailOutput.setTextContent("\"sendEMail\"");
                emailOutputEntry.addChildElement(sendEmailOutput);
                rule.addChildElement(emailOutputEntry);
            }

            decisionTable.addChildElement(rule);
        }

        try {
            Dmn.validateModel(modelInstance);
            String modelString = Dmn.convertToString(modelInstance);
            log.info("modelString " + modelString);

            evaluateDecisionTable(modelInstance);
        } catch (ModelValidationException e) {
            log.error("Error generating DMN " + e.getMessage());
            e.printStackTrace();
            throw new Exception("Error generating DMN " + e.getMessage());
        }
    }


    private void evaluateDecisionTable(DmnModelInstance modelInstance) {

        DmnEngine dmnEngine = DmnEngineConfiguration.createDefaultDmnEngineConfiguration().buildEngine();
        DmnDecision decision2 = dmnEngine.parseDecision("decisionGenerated", modelInstance);

        Iterable<CommonEntity> commonEntities = dbServer.getCommonData();
/*
//TODO  Habría que cargar en forma de variableMap los datos de COMMONDATAS para que los evalue el motor en base a la tabla de decisiones creada
        VariableMap variables = Variables
                .createVariables()
                .putValue("season", "Summer")
                .putValue("guestCount", 9);
        DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision2, variables);

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
*/
    }

}
