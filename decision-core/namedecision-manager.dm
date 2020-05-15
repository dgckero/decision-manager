<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<definitions id="definition-0" name="definition-name" namespace="http://camunda.org/schema/1.0/dmn" xmlns="http://www.omg.org/spec/DMN/20151101/dmn.xsd">
  <decision id="decision-0" name="decision-name">
    <decisionTable hitPolicy="COLLECT" id="decisionTable-name">
      <input id="input_bbae862c-8030-4b1e-aacb-fc287468a8a5">
        <inputExpression id="inputExpression_name" typeRef="filterclass">
          <text>name</text>
        </inputExpression>
      </input>
      <output id="output1" label="rule matched?" typeRef="string"/>
      <rule id="rule_6173a0d3-87ae-40f9-be16-92047a82936b">
        <inputEntry id="inputEntry_79cdab47-f497-48d7-8484-1c9cd25ca952" label="name">
          <text>"value"</text>
        </inputEntry>
        <outputEntry id="Accepted" label="Accepted">
          <text>"Accepted"</text>
        </outputEntry>
      </rule>
    </decisionTable>
  </decision>
</definitions>
