<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<definitions id="definition-0" name="definition-name" namespace="http://camunda.org/schema/1.0/dmn" xmlns="http://www.omg.org/spec/DMN/20151101/dmn.xsd">
  <decision id="decision-0" name="decision-name">
    <decisionTable hitPolicy="COLLECT" id="decisionTable-name">
      <input id="input_ff6257b0-8611-41db-a565-8d419e612f27">
        <inputExpression id="inputExpression_name" typeRef="filterclass">
          <text>name</text>
        </inputExpression>
      </input>
      <output id="output1" label="rule matched?" typeRef="string"/>
      <rule id="rule_81dd79f1-6418-4193-bbda-1b9c585251c6">
        <inputEntry id="inputEntry_eae4d969-ffdd-43cb-a6e3-222d22f41a06" label="name">
          <text>"value"</text>
        </inputEntry>
        <outputEntry id="Accepted" label="Accepted">
          <text>"Accepted"</text>
        </outputEntry>
      </rule>
    </decisionTable>
  </decision>
</definitions>
