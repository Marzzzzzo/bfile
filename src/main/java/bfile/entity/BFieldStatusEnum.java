package bfile.entity;

/**
 * 状态量字段枚举
 * Created by chenjingsi on 16-11-22.
 */
public enum BFieldStatusEnum {
    TIMESTAMP("timestamp"),
    GRID_UA("grid_ua"),
    GRID_UB("grid_ub"),
    GRID_UC("grid_uc"),
    GRID_I1("并网电流I1"),
    GRID_I2("并网电流I2"),
    GRID_I3("并网电流I3"),
    GRID_FREQUENCY_1("grid_frequency_1"),
    GRID_ACTIVE_POWER("电网有功功率"),
    GRID_REACTIVE_POWER("电网无功功率"),
    CONVERTER_POWER_1("converter_power_1"),
    CONVERTER_POWER_2("converter_power_2"),
    CONVERTER_TORQUE_1("converter_torque_1"),
    CONVERTER_TORQUE_2("converter_torque_2"),
    CONVERTER_SPEED_1("converter_speed_1"),
    CONVERTER_SPEED_2("converter_speed_2"),
    CONVERTER_REACTIVE_POWER_1("converter_reactive_power_1"),
    CONVERTER_REACTIVE_POWER_2("converter_reactive_power_2"),
    TORQUE_REFERENCE("torque_reference"),
    Q_POWER_REFERENCE("q_power_reference"),
    ACCELERATION_NACELLE_X("x方向加速度"),
    ACCELERATION_NACELLE_Y("y方向加速度"),
    ACCELERATION_NACELLE_EFFECTIVE_VALUE("有效加速度"),
    GENERATOR_SPEED_TO_MAIN_RUN_UP("generator_speed_to_main_run_up"),
    OVERSPEED_MODUL_GENERATOR_SPEED_SIGNAL_1("叶轮转速1"),
    OVERSPEED_MODUL_GENERATOR_SPEED_SIGNAL_2("叶轮转速2"),
    YAW_POSITION("偏航位置"),
    WIND_SPEED("风速"),
    PLC_CYCLE_TIME_MS("plc_cycle_time_ms"),
    PITCH_CAPACITOR_VOLTAGE_1("pitch_capacitor_voltage_1"),
    PITCH_CAPACITOR_VOLTAGE_2("pitch_capacitor_voltage_2"),
    PITCH_CAPACITOR_VOLTAGE_3("pitch_capacitor_voltage_3"),
    PITCH_SPEED_MOMENTARY_BLADE_1("pitch_speed_momentary_blade_1"),
    PITCH_SPEED_MOMENTARY_BLADE_2("pitch_speed_momentary_blade_2"),
    PITCH_SPEED_MOMENTARY_BLADE_3("pitch_speed_momentary_blade_3"),
    PITCH_POSITION_BLADE_1("变桨位置1"),
    PITCH_POSITION_BLADE_2("变桨位置2"),
    PITCH_POSITION_BLADE_3("变桨位置3"),
    PITCH_DCDC_VOLTAGE_1("pitch_dcdc_voltage_1"),
    PITCH_DCDC_VOLTAGE_2("pitch_dcdc_voltage_2"),
    PITCH_DCDC_VOLTAGE_3("pitch_dcdc_voltage_3"),
    PITCH_ERROR_CODE1_SYS_1("pitch_error_code1_sys_1"),
    PITCH_ERROR_CODE2_SYS_1("pitch_error_code2_sys_1"),
    PITCH_ERROR_CODE3_SYS_1("pitch_error_code3_sys_1"),
    PITCH_ERROR_CODE1_SYS_2("pitch_error_code1_sys_2"),
    PITCH_ERROR_CODE2_SYS_2("pitch_error_code2_sys_2"),
    PITCH_ERROR_CODE3_SYS_2("pitch_error_code3_sys_2"),
    PITCH_ERROR_CODE1_SYS_3("pitch_error_code1_sys_3"),
    PITCH_ERROR_CODE2_SYS_3("pitch_error_code2_sys_3"),
    PITCH_ERROR_CODE3_SYS_3("pitch_error_code3_sys_3"),
    PITCH_CHARGE_ERROR_CODE1_SYS_1("pitch_charge_error_code1_sys_1"),
    PITCH_CHARGE_ERROR_CODE2_SYS_1("pitch_charge_error_code2_sys_1"),
    PITCH_CHARGE_ERROR_CODE3_SYS_1("pitch_charge_error_code3_sys_1"),
    PITCH_CHARGE_STATUS1_SYS_1("pitch_charge_status1_sys_1"),
    PITCH_CHARGE_STATUS2_SYS_1("pitch_charge_status2_sys_1"),
    PITCH_CHARGE_STATUS3_SYS_1("pitch_charge_status3_sys_1"),
    PITCH_CHARGE_ERROR_CODE1_SYS_2("pitch_charge_error_code1_sys_2"),
    PITCH_CHARGE_ERROR_CODE2_SYS_2("pitch_charge_error_code2_sys_2"),
    PITCH_CHARGE_ERROR_CODE3_SYS_2("pitch_charge_error_code3_sys_2"),
    PITCH_CHARGE_STATUS1_SYS_2("pitch_charge_status1_sys_2"),
    PITCH_CHARGE_STATUS2_SYS_2("pitch_charge_status2_sys_2"),
    PITCH_CHARGE_STATUS3_SYS_2("pitch_charge_status3_sys_2"),
    PITCH_CHARGE_ERROR_CODE1_SYS_3("pitch_charge_error_code1_sys_3"),
    PITCH_CHARGE_ERROR_CODE2_SYS_3("pitch_charge_error_code2_sys_3"),
    PITCH_CHARGE_ERROR_CODE3_SYS_3("pitch_charge_error_code3_sys_3"),
    PITCH_CHARGE_STATUS1_SYS_3("pitch_charge_status1_sys_3"),
    PITCH_CHARGE_STATUS2_SYS_3("pitch_charge_status2_sys_3"),
    PITCH_CHARGE_STATUS3_SYS_3("pitch_charge_status3_sys_3"),
    PITCH_MOTOR_TEMP_1("pitch_motor_temp_1"),
    PITCH_MOTOR_TEMP_2("pitch_motor_temp_2"),
    PITCH_MOTOR_TEMP_3("pitch_motor_temp_3"),
    PITCH_AC2_STATE_WORD_1("pitch_ac2_state_word_1"),
    PITCH_AC2_STATE_WORD_2("pitch_ac2_state_word_2"),
    PITCH_AC2_STATE_WORD_3("pitch_ac2_state_word_3"),
    PITCH_AC2_ALARM_CODE_1("pitch_ac2_alarm_code_1"),
    PITCH_AC2_ALARM_CODE_2("pitch_ac2_alarm_code_2"),
    PITCH_AC2_ALARM_CODE_3("pitch_ac2_alarm_code_3"),
    PITCH_AC2_MOTOR_CURRENT_1("pitch_ac2_motor_current_1"),
    PITCH_AC2_MOTOR_CURRENT_2("pitch_ac2_motor_current_2"),
    PITCH_AC2_MOTOR_CURRENT_3("pitch_ac2_motor_current_3"),
    PITCH_AC2_MOTOR_TEMP_1("pitch_ac2_motor_temp_1"),
    PITCH_AC2_MOTOR_TEMP_2("pitch_ac2_motor_temp_2"),
    PITCH_AC2_MOTOR_TEMP_3("pitch_ac2_motor_temp_3"),
    PITCH_BRAKE_VOLTAGE_1("pitch_brake_voltage_1"),
    PITCH_BRAKE_VOLTAGE_2("pitch_brake_voltage_2"),
    PITCH_BRAKE_VOLTAGE_3("pitch_brake_voltage_3"),
    GH_CONTROL_TORQUE_DEMAND("gh_control_torque_demand"),
    GH_PITCH_RATE_DEMAND_1("变桨速度需求1"),
    GH_PITCH_RATE_DEMAND_2("变桨速度需求2"),
    GH_PITCH_RATE_DEMAND_3("变桨速度需求3"),




    ;
    private String field;	//field
    private String fieldCN; //fieldCN

    private BFieldStatusEnum(String field) {
        this.field = field;

    }


    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
