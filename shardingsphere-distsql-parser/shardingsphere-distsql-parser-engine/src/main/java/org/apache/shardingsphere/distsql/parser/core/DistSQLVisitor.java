/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.distsql.parser.core;

import com.google.common.base.Joiner;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AddResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.AlgorithmPropertyContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.BindTableRulesDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CheckScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CreateShardingBindingTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CreateShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.CreateShardingTableRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DataSourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropResourceContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.DropScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.FunctionDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ResetScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShardingTableRuleDefinitionContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowResourcesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowScalingJobListContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowScalingJobStatusContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowShardingRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.StartScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.StopScalingJobContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.TableNameContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowShardingBroadcastTableRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowReadwriteSplittingRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowDatabaseDiscoveryRulesContext;
import org.apache.shardingsphere.distsql.parser.autogen.DistSQLStatementParser.ShowEncryptRulesContext;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.FunctionSegment;
import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.DatabaseDiscoveryRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptColumnSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.EncryptRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ShardingBindingTableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.CheckScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.DropScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ResetScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ShowScalingJobListStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.ShowScalingJobStatusStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.StartScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.impl.StopScalingJobStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.AlterShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.AddResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropEncryptRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropResourceStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.drop.impl.DropShardingTableRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowDatabaseDiscoveryRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowEncryptRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowReadwriteSplittingRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowResourcesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowShardingBindingTableRulesStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowShardingBroadcastTableRulesStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Dist SQL visitor.
 */
public final class DistSQLVisitor extends DistSQLStatementBaseVisitor<ASTNode> {

    @Override
    public ASTNode visitAddResource(final AddResourceContext ctx) {
        Collection<DataSourceSegment> connectionInfos = new LinkedList<>();
        for (DataSourceContext each : ctx.dataSource()) {
            connectionInfos.add((DataSourceSegment) visit(each));
        }
        return new AddResourceStatement(connectionInfos);
    }
    
    @Override
    public ASTNode visitDataSource(final DataSourceContext ctx) {
        DataSourceSegment result = new DataSourceSegment();
        result.setName(ctx.dataSourceName().getText());
        result.setHostName(ctx.hostName().getText());
        result.setPort(ctx.port().getText());
        result.setDb(ctx.dbName().getText());
        result.setUser(ctx.user().getText());
        result.setPassword(null == ctx.password() ? "" : ctx.password().getText());
        return result;
    }
    
    @Override
    public ASTNode visitCreateShardingTableRule(final CreateShardingTableRuleContext ctx) {
        CreateShardingTableRuleStatement result = new CreateShardingTableRuleStatement();
        for (ShardingTableRuleDefinitionContext each : ctx.shardingTableRuleDefinition()) {
            result.getTables().add((TableRuleSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCreateShardingBindingTableRules(final CreateShardingBindingTableRulesContext ctx) {
        CreateShardingBindingTableRulesStatement result = new CreateShardingBindingTableRulesStatement();
        for (BindTableRulesDefinitionContext each : ctx.bindTableRulesDefinition()) {
            ShardingBindingTableRuleSegment segment = new ShardingBindingTableRuleSegment();
            segment.setTables(Joiner.on(",")
                    .join(each.tableName().stream().map(t -> new IdentifierValue(t.getText()).getValue()).collect(Collectors.toList())));
            result.getRules().add(segment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitDropResource(final DropResourceContext ctx) {
        DropResourceStatement result = new DropResourceStatement();
        for (TerminalNode each : ctx.IDENTIFIER()) {
            result.getResourceNames().add(each.getText());
        }
        return result;
    }

    @Override
    public ASTNode visitCreateShardingBroadcastTableRules(final CreateShardingBroadcastTableRulesContext ctx) {
        CreateShardingBroadcastTableRulesStatement result = new CreateShardingBroadcastTableRulesStatement();
        result.getTables().addAll(ctx.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList()));
        return result;
    }

    @Override
    public ASTNode visitAlterShardingTableRule(final DistSQLStatementParser.AlterShardingTableRuleContext ctx) {
        AlterShardingTableRuleStatement result = new AlterShardingTableRuleStatement();
        for (ShardingTableRuleDefinitionContext each : ctx.shardingTableRuleDefinition()) {
            result.getTables().add((TableRuleSegment) visit(each));
        }
        return result;
    }

    @Override
    public ASTNode visitShowShardingBroadcastTableRules(final ShowShardingBroadcastTableRulesContext ctx) {
        return new ShowShardingBroadcastTableRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }

    @Override
    public ASTNode visitAlterShardingBindingTableRules(final DistSQLStatementParser.AlterShardingBindingTableRulesContext ctx) {
        AlterShardingBindingTableRulesStatement result = new AlterShardingBindingTableRulesStatement();
        for (BindTableRulesDefinitionContext each : ctx.bindTableRulesDefinition()) {
            ShardingBindingTableRuleSegment segment = new ShardingBindingTableRuleSegment();
            segment.setTables(Joiner.on(",")
                    .join(each.tableName().stream().map(t -> new IdentifierValue(t.getText()).getValue()).collect(Collectors.toList())));
            result.getRules().add(segment);
        }
        return result;
    }

    @Override
    public ASTNode visitAlterShardingBroadcastTableRules(final DistSQLStatementParser.AlterShardingBroadcastTableRulesContext ctx) {
        AlterShardingBroadcastTableRulesStatement result = new AlterShardingBroadcastTableRulesStatement();
        result.getTables().addAll(ctx.IDENTIFIER().stream().map(ParseTree::getText).collect(Collectors.toList()));
        return result;
    }

    @Override
    public ASTNode visitCreateReadwriteSplittingRule(final DistSQLStatementParser.CreateReadwriteSplittingRuleContext ctx) {
        return new CreateReadwriteSplittingRuleStatement(ctx.readwriteSplittingRuleDefinition()
                .stream().map(each -> (ReadwriteSplittingRuleSegment) visit(each)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitReadwriteSplittingRuleDefinition(final DistSQLStatementParser.ReadwriteSplittingRuleDefinitionContext ctx) {
        ReadwriteSplittingRuleSegment result = (ReadwriteSplittingRuleSegment) (null != ctx.dynamicReadwriteSplittingRuleDefinition()
                        ? visit(ctx.dynamicReadwriteSplittingRuleDefinition())
                        : visit(ctx.staticReadwriteSplittingRuleDefinition()));
        Properties props = new Properties();
        if (null != ctx.functionDefinition().algorithmProperties()) {
            ctx.functionDefinition().algorithmProperties().algorithmProperty()
                    .forEach(each -> props.setProperty(each.key.getText(), each.value.getText()));
        }
        result.setName(ctx.ruleName().getText());
        result.setLoadBalancer(ctx.functionDefinition().functionName().getText());
        result.setProps(props);
        return result;
    }

    @Override
    public ASTNode visitStaticReadwriteSplittingRuleDefinition(final DistSQLStatementParser.StaticReadwriteSplittingRuleDefinitionContext ctx) {
        ReadwriteSplittingRuleSegment result = new ReadwriteSplittingRuleSegment();
        result.setWriteDataSource(ctx.writeResourceName().getText());
        result.setReadDataSources(ctx.resourceName().stream().map(each -> each.getText()).collect(Collectors.toList()));
        return result;
    }

    @Override
    public ASTNode visitDynamicReadwriteSplittingRuleDefinition(final DistSQLStatementParser.DynamicReadwriteSplittingRuleDefinitionContext ctx) {
        ReadwriteSplittingRuleSegment result = new ReadwriteSplittingRuleSegment();
        result.setAutoAwareResource(ctx.IDENTIFIER().getText());
        return result;
    }

    @Override
    public ASTNode visitAlterReadwriteSplittingRule(final DistSQLStatementParser.AlterReadwriteSplittingRuleContext ctx) {
        return new AlterReadwriteSplittingRuleStatement(ctx.readwriteSplittingRuleDefinition()
                .stream().map(each -> (ReadwriteSplittingRuleSegment) visit(each)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitDropShardingTableRule(final DistSQLStatementParser.DropShardingTableRuleContext ctx) {
        DropShardingTableRuleStatement result = new DropShardingTableRuleStatement();
        for (TableNameContext each : ctx.tableName()) {
            result.getTableNames().add((TableNameSegment) visit(each));
        }
        return result;
    }

    @Override
    public ASTNode visitDropShardingBindingTableRules(final DistSQLStatementParser.DropShardingBindingTableRulesContext ctx) {
        return new DropShardingBindingTableRulesStatement();
    }

    @Override
    public ASTNode visitDropShardingBroadcastTableRules(final DistSQLStatementParser.DropShardingBroadcastTableRulesContext ctx) {
        return new DropShardingBroadcastTableRulesStatement();
    }
    
    @Override
    public ASTNode visitShardingTableRuleDefinition(final ShardingTableRuleDefinitionContext ctx) {
        TableRuleSegment result = new TableRuleSegment();
        result.setLogicTable(ctx.tableName().getText());
        Collection<String> dataSources = new LinkedList<>();
        if (null != ctx.resources()) {
            for (TerminalNode each : ctx.resources().IDENTIFIER()) {
                dataSources.add(new IdentifierValue(each.getText()).getValue());
            }
        }
        result.setDataSources(dataSources);
        if (null != ctx.functionDefinition()) {
            result.setTableStrategy((FunctionSegment) visit(ctx.functionDefinition()));
            result.setTableStrategyColumn(ctx.shardingColumn().columnName().getText());
        }
        if (null != ctx.keyGenerateStrategy()) {
            result.setKeyGenerateStrategy((FunctionSegment) visit(ctx.keyGenerateStrategy().functionDefinition()));
            result.setKeyGenerateStrategyColumn(ctx.keyGenerateStrategy().columnName().getText());
        }
        return result;
    }
    
    @Override
    public ASTNode visitFunctionDefinition(final FunctionDefinitionContext ctx) {
        FunctionSegment result = new FunctionSegment();
        result.setAlgorithmName(ctx.functionName().getText());
        Properties algorithmProps = new Properties();
        if (null != ctx.algorithmProperties()) {
            for (AlgorithmPropertyContext each : ctx.algorithmProperties().algorithmProperty()) {
                algorithmProps.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
            }
        }
        result.setAlgorithmProps(algorithmProps);
        return result;
    }

    @Override
    public ASTNode visitCreateDatabaseDiscoveryRule(final DistSQLStatementParser.CreateDatabaseDiscoveryRuleContext ctx) {
        return new CreateDatabaseDiscoveryRuleStatement(ctx.databaseDiscoveryRuleDefinition().stream().map(each -> (DatabaseDiscoveryRuleSegment) visit(each)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitDatabaseDiscoveryRuleDefinition(final DistSQLStatementParser.DatabaseDiscoveryRuleDefinitionContext ctx) {
        DatabaseDiscoveryRuleSegment result = new DatabaseDiscoveryRuleSegment();
        result.setName(ctx.ruleName().getText());
        result.setDataSources(ctx.resources().IDENTIFIER().stream().map(each -> new IdentifierValue(each.getText()).getValue()).collect(Collectors.toList()));
        result.setDiscoveryTypeName(ctx.functionDefinition().functionName().getText());
        result.setProps(buildAlgorithmProperties(ctx.functionDefinition().algorithmProperties()));
        return result;
    }

    @Override
    public ASTNode visitAlterDatabaseDiscoveryRule(final DistSQLStatementParser.AlterDatabaseDiscoveryRuleContext ctx) {
        return new AlterDatabaseDiscoveryRuleStatement(ctx.databaseDiscoveryRuleDefinition().stream().map(each -> (DatabaseDiscoveryRuleSegment) visit(each)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitDropDatabaseDiscoveryRule(final DistSQLStatementParser.DropDatabaseDiscoveryRuleContext ctx) {
        DropDatabaseDiscoveryRuleStatement result = new DropDatabaseDiscoveryRuleStatement();
        result.getRuleNames().addAll(ctx.IDENTIFIER().stream().map(TerminalNode::getText).collect(Collectors.toList()));
        return result;
    }

    @Override
    public ASTNode visitCreateEncryptRule(final DistSQLStatementParser.CreateEncryptRuleContext ctx) {
        return new CreateEncryptRuleStatement(ctx.encryptRuleDefinition()
                .stream().map(each -> (EncryptRuleSegment) visit(each)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitEncryptRuleDefinition(final DistSQLStatementParser.EncryptRuleDefinitionContext ctx) {
        return new EncryptRuleSegment(ctx.tableName().getText(), ctx.columnDefinition()
                .stream().map(each -> (EncryptColumnSegment) visit(each)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitColumnDefinition(final DistSQLStatementParser.ColumnDefinitionContext ctx) {
        EncryptColumnSegment result = new EncryptColumnSegment();
        result.setName(ctx.columnName().getText());
        result.setCipherColumn(ctx.cipherColumnName().getText());
        if (Objects.nonNull(ctx.plainColumnName())) {
            result.setPlainColumn(ctx.plainColumnName().getText());
        }
        result.setEncryptor((FunctionSegment) visit(ctx.functionDefinition()));
        return result;
    }

    @Override
    public ASTNode visitAlterEncryptRule(final DistSQLStatementParser.AlterEncryptRuleContext ctx) {
        return new AlterEncryptRuleStatement(ctx.encryptRuleDefinition()
                .stream().map(each -> (EncryptRuleSegment) visit(each)).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitDropEncryptRule(final DistSQLStatementParser.DropEncryptRuleContext ctx) {
        return new DropEncryptRuleStatement(ctx.IDENTIFIER().stream().map(TerminalNode::getText).collect(Collectors.toList()));
    }

    @Override
    public ASTNode visitTableName(final TableNameContext ctx) {
        return new TableNameSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }

    @Override
    public ASTNode visitDropReadwriteSplittingRule(final DistSQLStatementParser.DropReadwriteSplittingRuleContext ctx) {
        DropReadwriteSplittingRuleStatement result = new DropReadwriteSplittingRuleStatement();
        result.getRuleNames().addAll(ctx.IDENTIFIER().stream().map(TerminalNode::getText).collect(Collectors.toList()));
        return result;
    }

    @Override
    public ASTNode visitShowShardingRule(final ShowShardingRuleContext ctx) {
        if (null != ctx.schemaName()) {
            return new ShowRuleStatement("sharding", (SchemaSegment) visitSchemaName(ctx.schemaName()));
        }
        return new ShowRuleStatement("sharding", null);
    }
    
    @Override
    public ASTNode visitShowResources(final ShowResourcesContext ctx) {
        return new ShowResourcesStatement(null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShowRule(final ShowRuleContext ctx) {
        return new ShowRuleStatement(ctx.ruleType().getText(), null == ctx.schemaName() ? null : (SchemaSegment) visit(ctx.schemaName()));
    }

    @Override
    public ASTNode visitShowShardingBindingTableRules(final DistSQLStatementParser.ShowShardingBindingTableRulesContext ctx) {
        return new ShowShardingBindingTableRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }

    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return new SchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), new IdentifierValue(ctx.getText()));
    }
    
    @Override
    public ASTNode visitShowScalingJobList(final ShowScalingJobListContext ctx) {
        return new ShowScalingJobListStatement();
    }
    
    @Override
    public ASTNode visitShowScalingJobStatus(final ShowScalingJobStatusContext ctx) {
        return new ShowScalingJobStatusStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitStartScalingJob(final StartScalingJobContext ctx) {
        return new StartScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitStopScalingJob(final StopScalingJobContext ctx) {
        return new StopScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitDropScalingJob(final DropScalingJobContext ctx) {
        return new DropScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }

    @Override
    public ASTNode visitShowReadwriteSplittingRules(final ShowReadwriteSplittingRulesContext ctx) {
        return new ShowReadwriteSplittingRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }

    @Override
    public ASTNode visitShowDatabaseDiscoveryRules(final ShowDatabaseDiscoveryRulesContext ctx) {
        return new ShowDatabaseDiscoveryRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }

    @Override
    public ASTNode visitShowEncryptRules(final ShowEncryptRulesContext ctx) {
        return new ShowEncryptRulesStatement(Objects.nonNull(ctx.schemaName()) ? (SchemaSegment) visit(ctx.schemaName()) : null);
    }

    @Override
    public ASTNode visitResetScalingJob(final ResetScalingJobContext ctx) {
        return new ResetScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }
    
    @Override
    public ASTNode visitCheckScalingJob(final CheckScalingJobContext ctx) {
        return new CheckScalingJobStatement(Long.parseLong(ctx.jobId().getText()));
    }

    private Properties buildAlgorithmProperties(final DistSQLStatementParser.AlgorithmPropertiesContext ctx) {
        Properties result = new Properties();
        for (AlgorithmPropertyContext each : ctx.algorithmProperty()) {
            result.setProperty(new IdentifierValue(each.key.getText()).getValue(), new IdentifierValue(each.value.getText()).getValue());
        }
        return result;
    }
}
