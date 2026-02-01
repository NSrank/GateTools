package org.plugin.gatetools.model;

/**
 * 传送门条件类
 * 用于存储传送门的各种条件配置
 * 
 * @author NSrank, Augment
 */
public class GateCondition {
    
    /**
     * 条件类型枚举
     */
    public enum ConditionType {
        EXPERIENCE("experience"),
        PERMISSION("permission"),
        MONEY("money"),
        TELEPORT("teleport");
        
        private final String key;
        
        ConditionType(String key) {
            this.key = key;
        }
        
        public String getKey() {
            return key;
        }
        
        public static ConditionType fromKey(String key) {
            for (ConditionType type : values()) {
                if (type.key.equals(key)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    /**
     * 判断器类型枚举
     */
    public enum JudgeType {
        SET("set"),
        COST("cost");
        
        private final String key;
        
        JudgeType(String key) {
            this.key = key;
        }
        
        public String getKey() {
            return key;
        }
        
        public static JudgeType fromKey(String key) {
            for (JudgeType type : values()) {
                if (type.key.equals(key)) {
                    return type;
                }
            }
            return null;
        }
    }
    
    /**
     * 比较操作符枚举
     */
    public enum CompareOperator {
        EQUAL("="),
        NOT_EQUAL("!"),
        GREATER(">"),
        GREATER_EQUAL(">="),
        LESS("<"),
        LESS_EQUAL("<=");
        
        private final String symbol;
        
        CompareOperator(String symbol) {
            this.symbol = symbol;
        }
        
        public String getSymbol() {
            return symbol;
        }
        
        public static CompareOperator fromSymbol(String symbol) {
            for (CompareOperator op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            return null;
        }
    }
    
    private final ConditionType conditionType;
    private final JudgeType judgeType;
    private final CompareOperator compareOperator;
    private final String value;
    
    /**
     * 构造函数
     * 
     * @param conditionType 条件类型
     * @param judgeType 判断器类型
     * @param compareOperator 比较操作符（可为null，用于cost类型）
     * @param value 值
     */
    public GateCondition(ConditionType conditionType, JudgeType judgeType, 
                        CompareOperator compareOperator, String value) {
        this.conditionType = conditionType;
        this.judgeType = judgeType;
        this.compareOperator = compareOperator;
        this.value = value;
    }
    
    /**
     * 创建set类型条件
     * 
     * @param conditionType 条件类型
     * @param compareOperator 比较操作符
     * @param value 值
     * @return GateCondition对象
     */
    public static GateCondition createSetCondition(ConditionType conditionType, 
                                                  CompareOperator compareOperator, String value) {
        return new GateCondition(conditionType, JudgeType.SET, compareOperator, value);
    }
    
    /**
     * 创建cost类型条件
     * 
     * @param conditionType 条件类型
     * @param value 值
     * @return GateCondition对象
     */
    public static GateCondition createCostCondition(ConditionType conditionType, String value) {
        return new GateCondition(conditionType, JudgeType.COST, null, value);
    }
    
    /**
     * 创建传送目标条件
     * 
     * @param location 传送位置
     * @return GateCondition对象
     */
    public static GateCondition createTeleportCondition(Location3D location) {
        return new GateCondition(ConditionType.TELEPORT, JudgeType.SET, null, location.toString());
    }
    
    // Getters
    public ConditionType getConditionType() { return conditionType; }
    public JudgeType getJudgeType() { return judgeType; }
    public CompareOperator getCompareOperator() { return compareOperator; }
    public String getValue() { return value; }
    
    /**
     * 获取传送位置（仅当条件类型为TELEPORT时有效）
     * 
     * @return Location3D对象，如果不是传送条件则返回null
     */
    public Location3D getTeleportLocation() {
        if (conditionType == ConditionType.TELEPORT) {
            try {
                return Location3D.fromString(value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(conditionType.getKey()).append(" ").append(judgeType.getKey());
        if (compareOperator != null) {
            sb.append(" ").append(compareOperator.getSymbol());
        }
        sb.append(" ").append(value);
        return sb.toString();
    }
}
