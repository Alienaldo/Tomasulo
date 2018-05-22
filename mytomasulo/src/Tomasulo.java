
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

/**
 * @author yanqing.qyq 2012-2015@USTC
 * 模板说明：该模板主要提供依赖Swing组件提供的JPanle，JFrame，JButton等提供的GUI。使用“监听器”模式监听各个Button的事件，从而根据具体事件执行不同方法。
 * Tomasulo算法核心需同学们自行完成，见说明（4）
 * 对于界面必须修改部分，见说明(1),(2),(3)
 *
 *  (1)说明：根据你的设计完善指令设置中的下拉框内容
 *	(2)说明：请根据你的设计指定各个面板（指令状态，保留站，Load部件，寄存器部件）的大小
 *	(3)说明：设置界面默认指令
 *	(4)说明： Tomasulo算法实现
 */

public class Tomasulo extends JFrame implements ActionListener{
    /*
     * 界面上有六个面板：
     * ins_set_panel : 指令设置
     * EX_time_set_panel : 执行时间设置
     * ins_state_panel : 指令状态
     * RS_panel : 保留站状态
     * Load_panel : Load部件
     * Registers_state_panel : 寄存器状态
     */
    private JPanel ins_set_panel,EX_time_set_panel,ins_state_panel,RS_panel,Load_panel,Registers_state_panel;

    /*
     * 四个操作按钮：步进，进5步，重置，执行
     */
    private JButton stepbut,step5but,resetbut,startbut;

    /*
     * 指令选择框
     */
    private JComboBox inst_typebox[]=new JComboBox[24];

    /*
     * 每个面板的名称
     */
    private JLabel inst_typel, timel, tl1,tl2,tl3,tl4,resl,regl,ldl,insl,stepsl;
    private int time[]=new int[4];

    /*
     * 部件执行时间的输入框
     */
    private JTextField tt1,tt2,tt3,tt4;

    private int intv[][]=new int[6][4],cnow,inst_typenow=0;
    private int cal[][]={{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0},{-1,0,0}};
    private int ld[][]={{0,0},{0,0},{0,0}};
    private int ff[]={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

    /*
     * (1)说明：根据你的设计完善指令设置中的下拉框内容
     * inst_type： 指令下拉框内容:"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"…………
     * regist_table：       目的寄存器下拉框内容:"F0","F2","F4","F6","F8" …………
     * rx：       源操作数寄存器内容:"R0","R1","R2","R3","R4","R5","R6","R7","R8","R9" …………
     * ix：       立即数下拉框内容:"0","1","2","3","4","5","6","7","8","9" …………
     */
    private String  inst_type[]={"NOP","L.D","ADD.D","SUB.D","MULT.D","DIV.D"},
            regist_table[]={"F0","F2","F4","F6","F8","F10","F12","F14","F16"
                    ,"F18","F20","F22","F24","F26","F28","F30","F32"},
            rx[]={"R0","R1","R2","R3","R4","R5","R6"},
            ix[]={"0","1","2","3","4","5","6","7","8","9","10","11",
            "12","13","14","15","16","17","18","19","20","21","22"};

    /*
     * (2)说明：请根据你的设计指定各个面板（指令状态，保留站，Load部件，寄存器部件）的大小
     * 		指令状态 面板
     * 		保留站 面板
     * 		Load部件 面板
     * 		寄存器 面板
     * 					的大小
     */
    private	String  my_inst_type[][]=new String[7][4], my_rs[][]=new String[6][8],
            my_load[][]=new String[4][4], my_regsters[][]=new String[3][17];
    private	JLabel  inst_typejl[][]=new JLabel[7][4], resjl[][]=new JLabel[6][8],
            ldjl[][]=new JLabel[4][4], regjl[][]=new JLabel[3][17];

    private int [][] instruction_buffer = new int[6][4];//存储从JComboBox中的字符串
    private Instruction [] instructions = new Instruction[6];
    private int instruction_length;
    private LoadUnit [] loadunits = new LoadUnit[3];
    private ReserveStation [] reservestations = new ReserveStation[5];
    private Register [] registers = new Register[16];
    private String [] memory = new String [16];
    private int memory_ip;
    private int ip;

    //构造方法
    public Tomasulo(){
        super("Tomasulo Simulator");

        //设置布局
        Container cp=getContentPane();
        FlowLayout layout=new FlowLayout();
        cp.setLayout(layout);

        //指令设置。GridLayout(int 指令条数, int 操作码+操作数, int hgap, int vgap)
        inst_typel = new JLabel("指令设置");
        ins_set_panel = new JPanel(new GridLayout(6,4,0,0));
        ins_set_panel.setPreferredSize(new Dimension(350, 150));
        ins_set_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        //操作按钮:执行，重设，步进，步进5步
        timel = new JLabel("执行时间设置");
        EX_time_set_panel = new JPanel(new GridLayout(2,4,0,0));
        EX_time_set_panel.setPreferredSize(new Dimension(280, 80));
        EX_time_set_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        //指令状态
        insl = new JLabel("指令状态");
        ins_state_panel = new JPanel(new GridLayout(7,4,0,0));
        ins_state_panel.setPreferredSize(new Dimension(420, 175));
        ins_state_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));


        //寄存器状态
        regl = new JLabel("寄存器");
        Registers_state_panel = new JPanel(new GridLayout(3,17,0,0));
        Registers_state_panel.setPreferredSize(new Dimension(740, 75));
        Registers_state_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        //保留站
        resl = new JLabel("保留站");
        RS_panel = new JPanel(new GridLayout(6,7,0,0));
        RS_panel.setPreferredSize(new Dimension(500, 150));
        RS_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        //Load部件
        ldl = new JLabel("Load部件");
        Load_panel = new JPanel(new GridLayout(4,4,0,0));
        Load_panel.setPreferredSize(new Dimension(300, 100));
        Load_panel.setBorder(new EtchedBorder(EtchedBorder.RAISED));

        tl1 = new JLabel("Load");
        tl2 = new JLabel("加/减");
        tl3 = new JLabel("乘法");
        tl4 = new JLabel("除法");

//操作按钮:执行，重设，步进，步进5步
        stepsl = new JLabel();
        stepsl.setPreferredSize(new Dimension(200, 30));
        stepsl.setHorizontalAlignment(SwingConstants.CENTER);
        stepsl.setBorder(new EtchedBorder(EtchedBorder.RAISED));
        stepbut = new JButton("步进");
        stepbut.addActionListener(this);
        step5but = new JButton("步进5步");
        step5but.addActionListener(this);
        startbut = new JButton("执行");
        startbut.addActionListener(this);
        resetbut= new JButton("重设");
        resetbut.addActionListener(this);
        tt1 = new JTextField("2");
        tt2 = new JTextField("2");
        tt3 = new JTextField("10");
        tt4 = new JTextField("40");

//指令设置
        /*
         * 设置指令选择框（操作码，操作数，立即数等）的default选择
         */
        for (int i=0;i<2;i++)
            for (int j=0;j<4;j++){
                if (j==0){
                    inst_typebox[i*4+j]=new JComboBox(inst_type);
                }
                else if (j==1){
                    inst_typebox[i*4+j]=new JComboBox(regist_table);
                }
                else if (j==2){
                    inst_typebox[i*4+j]=new JComboBox(ix);
                }
                else {
                    inst_typebox[i*4+j]=new JComboBox(rx);
                }
                inst_typebox[i*4+j].addActionListener(this);
                ins_set_panel.add(inst_typebox[i*4+j]);
            }
        for (int i=2;i<6;i++)
            for (int j=0;j<4;j++){
                if (j==0){
                    inst_typebox[i*4+j]=new JComboBox(inst_type);
                }
                else {
                    inst_typebox[i*4+j]=new JComboBox(regist_table);
                }
                inst_typebox[i*4+j].addActionListener(this);
                ins_set_panel.add(inst_typebox[i*4+j]);
            }
        /*
         * (3)说明：设置界面默认指令，根据你设计的指令，操作数等的选择范围进行设置。
         * 默认6条指令。待修改
         */
//		inst_typebox[0].setSelectedIndex(1);
//		inst_typebox[1].setSelectedIndex(4);
//		inst_typebox[2].setSelectedIndex(21);
//		inst_typebox[3].setSelectedIndex(3);
//
//		inst_typebox[4].setSelectedIndex(1);
//		inst_typebox[5].setSelectedIndex(2);
//		inst_typebox[6].setSelectedIndex(16);
//		inst_typebox[7].setSelectedIndex(4);
//
//		inst_typebox[8].setSelectedIndex(4);
//		inst_typebox[9].setSelectedIndex(1);
//		inst_typebox[10].setSelectedIndex(2);
//		inst_typebox[11].setSelectedIndex(3);
//
//		inst_typebox[12].setSelectedIndex(3);
//		inst_typebox[13].setSelectedIndex(5);
//		inst_typebox[14].setSelectedIndex(4);
//		inst_typebox[15].setSelectedIndex(2);
//
//		inst_typebox[16].setSelectedIndex(5);
//		inst_typebox[17].setSelectedIndex(6);
//		inst_typebox[18].setSelectedIndex(1);
//		inst_typebox[19].setSelectedIndex(4);
//
//		inst_typebox[20].setSelectedIndex(2);
//		inst_typebox[21].setSelectedIndex(4);
//		inst_typebox[22].setSelectedIndex(5);
//		inst_typebox[23].setSelectedIndex(2);

        inst_typebox[0].setSelectedIndex(1);
        inst_typebox[1].setSelectedIndex(3);
        inst_typebox[2].setSelectedIndex(21);
        inst_typebox[3].setSelectedIndex(2);

        inst_typebox[4].setSelectedIndex(1);
        inst_typebox[5].setSelectedIndex(1);
        inst_typebox[6].setSelectedIndex(20);
        inst_typebox[7].setSelectedIndex(3);

        inst_typebox[8].setSelectedIndex(4);
        inst_typebox[9].setSelectedIndex(0);
        inst_typebox[10].setSelectedIndex(1);
        inst_typebox[11].setSelectedIndex(2);

        inst_typebox[12].setSelectedIndex(3);
        inst_typebox[13].setSelectedIndex(4);
        inst_typebox[14].setSelectedIndex(3);
        inst_typebox[15].setSelectedIndex(1);

        inst_typebox[16].setSelectedIndex(5);
        inst_typebox[17].setSelectedIndex(5);
        inst_typebox[18].setSelectedIndex(0);
        inst_typebox[19].setSelectedIndex(3);

        inst_typebox[20].setSelectedIndex(2);
        inst_typebox[21].setSelectedIndex(3);
        inst_typebox[22].setSelectedIndex(4);
        inst_typebox[23].setSelectedIndex(1);

//执行时间设置
        EX_time_set_panel.add(tl1);
        EX_time_set_panel.add(tt1);
        EX_time_set_panel.add(tl2);
        EX_time_set_panel.add(tt2);
        EX_time_set_panel.add(tl3);
        EX_time_set_panel.add(tt3);
        EX_time_set_panel.add(tl4);
        EX_time_set_panel.add(tt4);

//指令状态设置
        for (int i=0;i<7;i++)
        {
            for (int j=0;j<4;j++){
                inst_typejl[i][j]=new JLabel(my_inst_type[i][j]);
                inst_typejl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
                ins_state_panel.add(inst_typejl[i][j]);
            }
        }
//保留站设置
        for (int i=0;i<6;i++)
        {
            for (int j=0;j<8;j++){
                resjl[i][j]=new JLabel(my_rs[i][j]);
                resjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
                RS_panel.add(resjl[i][j]);
            }
        }
//Load部件设置
        for (int i=0;i<4;i++)
        {
            for (int j=0;j<4;j++){
                ldjl[i][j]=new JLabel(my_load[i][j]);
                ldjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
                Load_panel.add(ldjl[i][j]);
            }
        }
//寄存器设置
        for (int i=0;i<3;i++)
        {
            for (int j=0;j<17;j++){
                regjl[i][j]=new JLabel(my_regsters[i][j]);
                regjl[i][j].setBorder(new EtchedBorder(EtchedBorder.RAISED));
                Registers_state_panel.add(regjl[i][j]);
            }
        }

//向容器添加以上部件
        cp.add(inst_typel);
        cp.add(ins_set_panel);
        cp.add(timel);
        cp.add(EX_time_set_panel);

        cp.add(startbut);
        cp.add(resetbut);
        cp.add(stepbut);
        cp.add(step5but);

        cp.add(Load_panel);
        cp.add(ldl);
        cp.add(RS_panel);
        cp.add(resl);
        cp.add(stepsl);
        cp.add(Registers_state_panel);
        cp.add(regl);
        cp.add(ins_state_panel);
        cp.add(insl);

        stepbut.setEnabled(false);
        step5but.setEnabled(false);
        ins_state_panel.setVisible(false);
        insl.setVisible(false);
        RS_panel.setVisible(false);
        ldl.setVisible(false);
        Load_panel.setVisible(false);
        resl.setVisible(false);
        stepsl.setVisible(false);
        Registers_state_panel.setVisible(false);
        regl.setVisible(false);
        setSize(820,620);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /*
     * 点击”执行“按钮后，根据选择的指令，初始化其他几个面板
     */
    public void init(){
        // get value
        for (int i=0;i<6;i++){
            intv[i][0]=inst_typebox[i*4].getSelectedIndex();
            if (intv[i][0]!=0){
                intv[i][1]=2*inst_typebox[i*4+1].getSelectedIndex();
                if (intv[i][0]==1){
                    intv[i][2]=inst_typebox[i*4+2].getSelectedIndex();
                    intv[i][3]=inst_typebox[i*4+3].getSelectedIndex();
                }
                else {
                    intv[i][2]=2*inst_typebox[i*4+2].getSelectedIndex();
                    intv[i][3]=2*inst_typebox[i*4+3].getSelectedIndex();
                }
            }
        }
        time[0]=Integer.parseInt(tt1.getText());
        time[1]=Integer.parseInt(tt2.getText());
        time[2]=Integer.parseInt(tt3.getText());
        time[3]=Integer.parseInt(tt4.getText());
        //System.out.println(time[0]);
        // set 0
        my_inst_type[0][0]="指令";
        my_inst_type[0][1]="流出";
        my_inst_type[0][2]="执行";
        my_inst_type[0][3]="写回";


        my_load[0][0]="名称";
        my_load[0][1]="Busy";
        my_load[0][2]="地址";
        my_load[0][3]="值";
        my_load[1][0]="Load1";
        my_load[2][0]="Load2";
        my_load[3][0]="Load3";
        my_load[1][1]="no";
        my_load[2][1]="no";
        my_load[3][1]="no";

        my_rs[0][0]="Time";
        my_rs[0][1]="名称";
        my_rs[0][2]="Busy";
        my_rs[0][3]="Op";
        my_rs[0][4]="Vj";
        my_rs[0][5]="Vk";
        my_rs[0][6]="Qj";
        my_rs[0][7]="Qk";
        my_rs[1][1]="Add1";
        my_rs[2][1]="Add2";
        my_rs[3][1]="Add3";
        my_rs[4][1]="Mult1";
        my_rs[5][1]="Mult2";
        my_rs[1][2]="no";
        my_rs[2][2]="no";
        my_rs[3][2]="no";
        my_rs[4][2]="no";
        my_rs[5][2]="no";

        my_regsters[0][0]="字段";
        for (int i=1;i<17;i++){
            //System.out.print(i+" "+regist_table[i-1];
            my_regsters[0][i]=regist_table[i-1];

        }
        my_regsters[1][0]="状态";
        my_regsters[2][0]="值";

        for (int i=1;i<7;i++)
            for (int j=0;j<4;j++){
                if (j==0){
                    int temp=i-1;
                    String disp;
                    disp = inst_type[inst_typebox[temp*4].getSelectedIndex()]+" ";
                    if (inst_typebox[temp*4].getSelectedIndex()==0) disp=disp;
                    else if (inst_typebox[temp*4].getSelectedIndex()==1){
                        disp=disp+regist_table[inst_typebox[temp*4+1].getSelectedIndex()]+','+ix[inst_typebox[temp*4+2].getSelectedIndex()]+'('+rx[inst_typebox[temp*4+3].getSelectedIndex()]+')';
                    }
                    else {
                        disp=disp+regist_table[inst_typebox[temp*4+1].getSelectedIndex()]+','+regist_table[inst_typebox[temp*4+2].getSelectedIndex()]+','+regist_table[inst_typebox[temp*4+3].getSelectedIndex()];
                    }
                    my_inst_type[i][j]=disp;
                }
                else my_inst_type[i][j]="";
            }
        for (int i=1;i<6;i++)
            for (int j=0;j<8;j++)if (j!=1&&j!=2){
                my_rs[i][j]="";
            }
        for (int i=1;i<4;i++)
            for (int j=2;j<4;j++){
                my_load[i][j]="";
            }
        for (int i=1;i<3;i++)
            for (int j=1;j<17;j++){
                my_regsters[i][j]="";
            }
        inst_typenow=0;
        for (int i=0;i<5;i++){
            for (int j=1;j<3;j++) cal[i][j]=0;
            cal[i][0]=-1;
        }
        for (int i=0;i<3;i++)
            for (int j=0;j<2;j++) ld[i][j]=0;
        for (int i=0;i<17;i++) ff[i]=0;


    }

    /*
     * 点击操作按钮后，用于显示结果
     */
    public void display(){
        for (int i=0;i<7;i++)
            for (int j=0;j<4;j++){
                inst_typejl[i][j].setText(my_inst_type[i][j]);
            }
        for (int i=0;i<6;i++)
            for (int j=0;j<8;j++){
                resjl[i][j].setText(my_rs[i][j]);
            }
        for (int i=0;i<4;i++)
            for (int j=0;j<4;j++){
                ldjl[i][j].setText(my_load[i][j]);
            }
        for (int i=0;i<3;i++)
            for (int j=0;j<17;j++){
                regjl[i][j].setText(my_regsters[i][j]);
            }
        stepsl.setText("当前周期："+String.valueOf(cnow));
    }
    public void translate(){
        translate_instruction();
        translate_load();
        translate_reserve();
        translate_register();
    }
    public void translate_register(){
        for(int i=0;i<=15;i++){
            Register r = registers[i];
            my_regsters[1][i+1] = "" + r.qi;
            my_regsters[2][i+1] = "" + r.value;
        }
    }
    public void translate_reserve(){
        for(int i=1;i<6;i++){
            ReserveStation rs = reservestations[i-1];
            if(rs.time>0){
                my_rs[i][0] = "" + rs.time;
            }
            else{
                my_rs[i][0] = "";
            }
            String busy = rs.busy?"yes":"no";
            my_rs[i][2] = "" + busy;
            my_rs[i][3] = "" + rs.op;
            my_rs[i][4] = "" + rs.vj;
            my_rs[i][5] = "" + rs.vk;
            my_rs[i][6] = "" + rs.qj;
            my_rs[i][7] = "" + rs.qk;
        }
    }
    public void translate_load(){
        for(int i=1;i<=3;i++){
            String busy = loadunits[i-1].busy?"yes":"no";
            my_load[i][1] = "" + busy;
            my_load[i][2] = "" + loadunits[i-1].address;
            my_load[i][3] = "" + loadunits[i-1].value;
        }
    }
    public void translate_instruction(){
        for(int i=1;i<=instruction_length;i++){
            if(instructions[i-1].issue_time == 0){

            }
            else if(instructions[i-1].execute_start_time == 0){
                my_inst_type[i][1] = "" + instructions[i-1].issue_time;
            }
            else if(instructions[i-1].execute_end_time == 0){
                my_inst_type[i][2] = instructions[i-1].execute_start_time + "~";
            }
            else if(instructions[i-1].writeback_time == 0){
                my_inst_type[i][2] += instructions[i-1].execute_end_time;
            }
            else{
                my_inst_type[i][3] = "" + instructions[i-1].writeback_time;
            }
        }
    }
    public void Init_Unit(){
        Init_Memory();
        Init_Instruction();
        Init_LU();
        Init_RS();
        Init_R();
    }
    public void Init_Memory(){
        memory_ip = 0;
        for(int i=0;i<16;i++){
            memory[i] = null;
        }
    }
    public void Init_R(){
        for(int i=0;i<16;i++){
            registers[i] = new Register("F"+2*i);
        }
    }
    public void Init_RS(){
        for(int i=1;i<=3;i++){
            reservestations[i-1] = new ReserveStation("Add"+i);
        }
        reservestations[3] = new ReserveStation("Mult1");
        reservestations[4] = new ReserveStation("Mult2");
    }
    public void Init_LU(){
        for(int i=1;i<=3;i++){
            loadunits[i-1] = new LoadUnit("Load"+i);
        }
    }
    public void Init_Instruction(){
        ip = 0;
        instruction_length = 0;
        int optype;
        for(int i=0;i<6;i++){
            instructions[instruction_length] = new Instruction();
            if(inst_typebox[i*4].getSelectedIndex()==0) {
                optype = inst_typebox[i*4].getSelectedIndex();
                instructions[instruction_length].optype = OpType.values()[optype];
            }
            else{
                optype = inst_typebox[i*4].getSelectedIndex();
                instructions[instruction_length].optype = OpType.values()[optype];
                instructions[instruction_length].o1 = 2*inst_typebox[i*4+1].getSelectedIndex();
                if(optype == 1){
                    instructions[instruction_length].o2 = inst_typebox[i*4+2].getSelectedIndex();
                    instructions[instruction_length].o3 = inst_typebox[i*4+3].getSelectedIndex();
                }
                else{
                    instructions[instruction_length].o2 = 2*inst_typebox[i*4+2].getSelectedIndex();
                    instructions[instruction_length].o3 = 2*inst_typebox[i*4+3].getSelectedIndex();
                }
            }
            instruction_length++;
        }
    }
    public void actionPerformed(ActionEvent e){
//点击“执行”按钮的监听器
        if (e.getSource()==startbut) {
            for (int i=0;i<24;i++) inst_typebox[i].setEnabled(false);
            tt1.setEnabled(false);tt2.setEnabled(false);
            tt3.setEnabled(false);tt4.setEnabled(false);
            stepbut.setEnabled(true);
            step5but.setEnabled(true);
            startbut.setEnabled(false);
            //根据指令设置的指令初始化其他的面板
            init();
            cnow=0;
            //展示其他面板
            display();
            ins_state_panel.setVisible(true);
            RS_panel.setVisible(true);
            Load_panel.setVisible(true);
            Registers_state_panel.setVisible(true);
            insl.setVisible(true);
            ldl.setVisible(true);
            resl.setVisible(true);
            stepsl.setVisible(true);
            regl.setVisible(true);
            Init_Unit();
        }
//点击“重置”按钮的监听器
        if (e.getSource()==resetbut) {
            for (int i=0;i<24;i++) inst_typebox[i].setEnabled(true);
            tt1.setEnabled(true);tt2.setEnabled(true);
            tt3.setEnabled(true);tt4.setEnabled(true);
            stepbut.setEnabled(false);
            step5but.setEnabled(false);
            startbut.setEnabled(true);
            ins_state_panel.setVisible(false);
            insl.setVisible(false);
            RS_panel.setVisible(false);
            ldl.setVisible(false);
            Load_panel.setVisible(false);
            resl.setVisible(false);
            stepsl.setVisible(false);
            Registers_state_panel.setVisible(false);
            regl.setVisible(false);
        }
//点击“步进”按钮的监听器
        if (e.getSource()==stepbut) {
            cnow++;
            core();
            display();
        }
//点击“进5步”按钮的监听器
        if (e.getSource()==step5but) {
            for (int i=0;i<5;i++){
                cnow++;
                core();
            }
            display();
        }

        for (int i=0;i<24;i=i+4)
        {
            if (e.getSource()==inst_typebox[i]) {
                if (inst_typebox[i].getSelectedIndex()==1){
                    inst_typebox[i+2].removeAllItems();
                    for (int j=0;j<ix.length;j++) inst_typebox[i+2].addItem(ix[j]);
                    inst_typebox[i+3].removeAllItems();
                    for (int j=0;j<rx.length;j++) inst_typebox[i+3].addItem(rx[j]);
                }
                else {
                    inst_typebox[i+2].removeAllItems();
                    for (int j=0;j<regist_table.length;j++) inst_typebox[i+2].addItem(regist_table[j]);
                    inst_typebox[i+3].removeAllItems();
                    for (int j=0;j<regist_table.length;j++) inst_typebox[i+3].addItem(regist_table[j]);
                }
            }
        }
    }
    /*
     * (4)说明： Tomasulo算法实现
     */
    public void core(){
        if(ip>=0){
            Execute_LU();
            Execute_RS();
            Issue(ip);
            Ready();
            ip++;
            translate();
        }
    }
    public int choice_time(OpType a){
        int ti = 0;
        switch(a){
            case LD:
                ti = time[0];
                break;
            case ADDD:
            case SUBD:
                ti = time[1];
                break;
            case MULTD:
                ti = time[2];
                break;
            case DIVD:
                ti = time[3];
                break;
        }
        return ti;
    }
    public void Execute_RS(){
        for(ReserveStation rs : reservestations){
            if(rs.busy){
                if(rs.instruction.execute_start_time==0){
                    if(rs.ready){
                        rs.instruction.execute_start_time = cnow;
                        rs.time = choice_time(rs.instruction.optype) - 1;
                    }
                }
                else if(rs.instruction.execute_end_time==0){
                    rs.time--;
                    if(rs.time == 0){
                        rs.instruction.execute_end_time = cnow;

                    }
                }
                else if(rs.instruction.writeback_time==0){
                    String new_value=null;
                    new_value = rs.vj.toString() + rs.vk.toString();
                    memory[memory_ip++] = new_value;

                    rs.instruction.writeback_time = cnow;
                    rs.busy = false;
                    rs.op.setLength(0);
                    rs.vj.setLength(0);
                    rs.vk.setLength(0);
                    rs.qj.setLength(0);
                    rs.qk.setLength(0);
                    write_back(rs,new_value);
                }
                else{

                }
            }
        }
    }
    public void Execute_LU(){
        for(LoadUnit lu : loadunits){
            if(lu.busy){
                if(lu.instruction.execute_start_time == 0){
                    lu.instruction.execute_start_time = cnow;

                    String new_address;
                    new_address = "R[R" + lu.instruction.o3 + "]+" + lu.address.toString();
                    lu.address.setLength(0);
                    lu.address.append(new_address);
                }
                else if(lu.instruction.execute_end_time == 0){
                    lu.instruction.execute_end_time = cnow;

                    String new_value;
                    new_value = "M[" + lu.address.toString() + "]";

                    lu.value.setLength(0);
                    lu.value.append(new_value);

                    memory[memory_ip++] = new_value;
                }
                else{
                    lu.instruction.writeback_time = cnow;
                    lu.busy = false;
                    write_back(lu,lu.value.toString());
//                    write_back(lu,"M"+memory_ip);
                    lu.address.setLength(0);
                    lu.value.setLength(0);
                }
            }
        }
    }
    public void write_back(Object obj,String value){
        String name = null;
        int i = 0;
        if(obj instanceof LoadUnit){
            name = ((LoadUnit) obj).name;
            for(Register r : registers){
                if(r.qi.toString().equals(((LoadUnit) obj).name)){
                    String new_value = null;
                    for(i=0;i<16;i++){
                        if(memory[i].equals(value)){
                            new_value = "M" + (i+1);
                            break;
                        }
                    }
                    r.value.setLength(0);
                    r.value.append(new_value);
//                    r.value.setLength(0);
//                    r.value.append(value);
                    break;
                }
            }
        }
        else if(obj instanceof  ReserveStation){
//            name = ((ReserveStation) obj).name;
//            for(Register r : registers){
//                if(r.qi.toString().equals(name)){
//                    r.value.setLength(0);
//                    r.value.append(value);
//                }
//            }
            name = ((ReserveStation) obj).name;
            for(Register r : registers){
                if(r.qi.toString().equals(name)){
                    String new_value = null;
                    for(i=0;i<16;i++){
                        if(memory[i]==null){
                            System.out.println(value);
                            System.out.println("F"+2*i);
                            continue;
                        }
                        if(memory[i].equals(value)){
                            new_value = "M" + (i+1);
                            break;
                        }
                    }
                    r.value.setLength(0);
                    r.value.append(new_value);
//                    r.value.setLength(0);
//                    r.value.append(value);
                    break;
                }
            }
        }
        else{

        }

        for(ReserveStation re : reservestations){
            if(re.qj.toString().equals(name)){
                re.vj.setLength(0);
                re.vj.append("M"+(i+1));
                re.qj.setLength(0);
            }
            if(re.qk.toString().equals(name)){
                re.vk.setLength(0);
                re.vk.append("M"+(i+1));
                re.qk.setLength(0);
            }
        }
    }
    public void Ready(){
        for(ReserveStation rs : reservestations){
            if(rs.busy && rs.instruction.execute_start_time == 0 && rs.qj.toString().equals("")
                    && rs.qk.toString().equals("")){
                rs.ready = true;
            }
        }
    }
    public void Issue(int ip){
        if(ip<instruction_length){
            switch(instructions[ip].optype){
                case LD:
                    for(LoadUnit lu : loadunits){
                        if(!lu.busy){
                            lu.busy = true;
                            instructions[ip].issue_time = cnow;
                            lu.instruction = instructions[ip];
                            lu.address.append(instructions[ip].o2);
                            Register r1 = registers[instructions[ip].o1/2];
                            r1.qi.setLength(0);
                            r1.qi.append(lu.name);
                            break;
                        }
                    }
                    break;
                case NOP:
                    break;
                default:
                    int start,end;
                    if(instructions[ip].optype==OpType.ADDD||instructions[ip].optype==OpType.SUBD){
                        start = 0;
                        end = 3;
                    }
                    else{
                        start = 3;
                        end = 5;
                    }
                    for(int i=start;i<end;i++){
                        ReserveStation rs = reservestations[i];
                        if(!rs.busy){
                            rs.busy = true;
                            instructions[ip].issue_time = cnow;
                            rs.instruction = instructions[ip];
                            rs.op.setLength(0);
                            rs.op.append(instructions[ip].optype);

                            Register r2 = registers[instructions[ip].o2/2];
                            if(r2.qi.toString().equals("")){
                                String name = "R["+r2.name+"]";
                                rs.vj.append(name);
                                rs.qj.setLength(0);
                            }
                            else if(r2.value.toString().equals("")){
                                String name = r2.qi.toString();
                                rs.qj.append(name);
                                rs.vj.setLength(0);
                            }
                            else{
                                String name = r2.value.toString();
                                rs.vj.setLength(0);
                                rs.vj.append(name);
                                rs.qj.setLength(0);
                            }

                            Register r3 = registers[instructions[ip].o3/2];
                            if(r3.qi.toString().equals("")){
                                String name = "R["+r3.name+"]";
                                rs.vk.append(name);
                                rs.qk.setLength(0);
                            }
                            else if(r3.value.toString().equals("")){
                                String name = r3.qi.toString();
                                rs.qk.append(name);
                                rs.vk.setLength(0);
                            }
                            else{
                                String name = r3.value.toString();
                                rs.vk.setLength(0);
                                rs.vk.append(name);
                                rs.qk.setLength(0);
                            }

                            Register r1 = registers[instructions[ip].o1/2];
                            r1.qi.setLength(0);
                            r1.qi.append(rs.name);
                            break;
                        }
                    }
                    break;
            }
        }
    }
    public enum OpType{
        NOP, LD, ADDD, SUBD, MULTD, DIVD;
    }
//    OpType int_to_enum(int i){
//        return OpType.values()[i];
//    }
    public class Register{
        public String name;
        public StringBuffer qi;
        public StringBuffer value;
        public Register(String name){
            this.name = name;
            qi = new StringBuffer("");
            value = new StringBuffer("");
        }
    }
    public class Instruction{
        public OpType optype;
        public int o1;
        public int o2;
        public int o3;
        public int issue_time=0;
        public int execute_start_time=0;
        public int execute_end_time=0;
        public int writeback_time=0;
    }
    public class LoadUnit{
        public String name;
        public boolean busy;
        public StringBuffer address;
        public StringBuffer value;
        public Instruction instruction;
        public LoadUnit(String name){
            this.name = name;
            this.busy = false;
            address = new StringBuffer("");
            value = new StringBuffer("");
        }
    }
    public class ReserveStation{
        public int time;
        public String name;
        public boolean busy;
        public StringBuffer op;
        public StringBuffer vj;
        public StringBuffer vk;
        public StringBuffer qj;
        public StringBuffer qk;
        public boolean ready;
        public Instruction instruction;
        public ReserveStation(String name){
            this.name = name;
            this.busy = false;
            op = new StringBuffer("");
            vj = new StringBuffer("");
            vk = new StringBuffer("");
            qj = new StringBuffer("");
            qk = new StringBuffer("");
            this.time = -1;
            ready = false;
        }
    }
    public static void main(String[] args) {
        new Tomasulo();
    }

}
