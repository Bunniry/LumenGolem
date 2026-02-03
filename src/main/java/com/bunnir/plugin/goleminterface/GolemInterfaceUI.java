package com.bunnir.plugin.goleminterface;

import com.bunnir.plugin.components.LumenGolemComponent;
import com.bunnir.plugin.jsonassets.LumenInstruction;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.ui.ScriptedBrushPage;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

public class GolemInterfaceUI extends InteractiveCustomUIPage<GolemInterfaceUI.Data> {
    LumenGolemComponent golem;
    public GolemInterfaceUI(@NonNullDecl PlayerRef playerRef, @NonNullDecl CustomPageLifetime lifetime, LumenGolemComponent golem) {
        super(playerRef, lifetime, Data.CODEC);
        this.golem = golem;
    }

    @Override
    public void build(@Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder, @Nonnull Store<EntityStore> store) {
        uiCommandBuilder.append("LumenGolemScript.ui");
        //uiCommandBuilder.set("#MyLabel.TextSpans", Message.raw("NewText"));

        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#NewVariableButton",
                EventData.of("Button", "Variable"), false);
        uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#NewFunctionButton",
                EventData.of("Button", "Function"), false);

        buildElements(uiCommandBuilder, uiEventBuilder);
    }

    @Override
    public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, Data data) {
        super.handleDataEvent(ref, store, data);
        if(data.Button != null)
            switch (data.Button) {
                //reload every time new instruction is added
                case "Variable" -> {
                    BakeElements(bakeType.ADD_VARIABLE, 0, LumenInstruction.Instruction_Codec.ADD, LumenGolemComponent.Type_Codec.BOOL);
                }
                case "Function" -> {
                    BakeElements(bakeType.ADD_FUNCTION, 0, LumenInstruction.Instruction_Codec.ADD, LumenGolemComponent.Type_Codec.BOOL);
                }
                case "SaveFunction" -> {
                    SaveFunction(data);
                }
                case "SaveVariable" -> {
                    SaveVariable(data,LumenGolemComponent.Type_Codec.valueOf(data.Type));
                }
                case "ChangeFunctionType" -> {
                    BakeElements(bakeType.MODIFY_FUNCTION,
                            Integer.parseInt(data.FuncIndex),
                            LumenInstruction.Instruction_Codec.valueOf(data.Type), LumenGolemComponent.Type_Codec.BOOL);
                }

                case "ChangeVariableType" -> {
                    BakeElements(bakeType.MODIFY_VARIABLE,
                            Integer.parseInt(data.FuncIndex),
                            LumenInstruction.Instruction_Codec.ADD, LumenGolemComponent.Type_Codec.valueOf(data.Type));
                }
                case "DeleteFunction" -> {

                    BakeElements(bakeType.REMOVE_FUNCTION, Integer.parseInt(data.FuncIndex), LumenInstruction.Instruction_Codec.ADD, LumenGolemComponent.Type_Codec.BOOL);
                }
                case "DeleteVariable" -> {
                    BakeElements(bakeType.REMOVE_VARIABLE, Integer.parseInt(data.FuncIndex), LumenInstruction.Instruction_Codec.ADD, LumenGolemComponent.Type_Codec.BOOL);

                }
            }

        UICommandBuilder uiCommandBuilder = new UICommandBuilder();
        UIEventBuilder uiEventBuilder = new UIEventBuilder();

        buildElements(uiCommandBuilder, uiEventBuilder);
        sendUpdate(uiCommandBuilder, uiEventBuilder, false);
    }

    enum bakeType {
        ADD_VARIABLE,
        ADD_FUNCTION,
        REMOVE_VARIABLE,
        REMOVE_FUNCTION,
        MODIFY_VARIABLE,
        MODIFY_FUNCTION,
    }

    void SaveFunction(Data data) {
        String name = data.Name;

        int new_index = 0;

        while(true) {
            boolean canBreak = true;
            for (int j = 0; j < FuncStrings.size(); j++) {
                if (j != Integer.parseInt(data.FuncIndex) && FuncStrings.get(j).equals(name)) {
                    new_index = new_index + 1;
                    name = name + "_" + new_index;
                    canBreak = false;
                    break;
                }
            }
            if(canBreak) break;
        }

        golem.instructions[Integer.parseInt(data.FuncIndex)].Name = name;

        for(int i = 0; i < 1000; i++) {
            if(data.Variables[i] == null)
                return;

            int RIndex = Integer.parseInt(data.RIndex);
            if(i < RIndex)
                for(int j = 0; j < VarStrings.size();j++) {
                    if (VarStrings.get(j).equals(data.Variables[i])) {
                        golem.instructions[Integer.parseInt(data.FuncIndex)].Pointers[i] = j;
                    }
                }
            else {
                //null pointer for function
                if (data.Variables[i].equals("-1"))

                    golem.instructions[Integer.parseInt(data.FuncIndex)].Pointers[i] = -1;
                else
                    for (int j = 0; j < FuncStrings.size(); j++) {
                        if (FuncStrings.get(j).equals(data.Variables[i])) {
                            golem.instructions[Integer.parseInt(data.FuncIndex)].Pointers[i] = j;
                        }
                    }
            }

        }
    }

    void SaveVariable(Data data, LumenGolemComponent.Type_Codec Var_Value) {

        String name = data.Name;

        int new_index = 0;

        while (true) {
            boolean canBreak = true;
            for (int j = 0; j < VarStrings.size(); j++) {
                if (j != Integer.parseInt(data.FuncIndex) && VarStrings.get(j).equals(name)) {
                    new_index = new_index + 1;
                    name = name + "_" + new_index;
                    canBreak = false;
                    break;
                }
            }
            if (canBreak) break;
        }

        int index = Integer.parseInt(data.FuncIndex);

        golem.data_names[index] = name;

        switch (Var_Value) {
            case POS -> {
                double x = 0;
                double y = 0;
                double z = 0;
                try {
                    x = Double.parseDouble(data.Variables[0]);
                } catch (Exception e) {

                }
                try {
                    y = Double.parseDouble(data.Variables[1]);
                } catch (Exception e) {

                }
                try {
                    z = Double.parseDouble(data.Variables[2]);
                } catch (Exception e) {

                }

                golem.data[index] = new Vector3d(x, y, z);
            }
            case BOOL -> {
                try {
                    golem.data[index] = Boolean.parseBoolean(data.Variables[0]);
                } catch (Exception e) {
                    golem.data[index] = false;
                }
            }
            case STRING -> golem.data[index] = data.Variables[0];
            case FLOAT -> {
                try {
                    golem.data[index] = Float.parseFloat(data.Variables[0]);
                } catch (Exception e) {
                    golem.data[index] = 0.0;
                }
            }
        }
    }

    void RemoveNullPointers_Variable(int pointer) {
        for(int i = 0; i < golem.instructions.length;i++)
        {
            switch (golem.instructions[i].ID) {case ADD:
                case SUBTRACT:
                case MULTIPLY:
                case DIVIDE:
                case MODULO:
                case LESS:
                case LESSOREQUAL:
                case EQUALS:
                case DISTANCE:
                case NOTEQUAL:
                {
                    for(int j = 0; j < 3; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;
                }
                break;
                case ROUND:
                {
                    for(int j = 0; j < 2; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;
                }
                break;
                case IFELSE:
                    for(int j = 0; j < 1; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;

                    break;
                case GETPOSITION:
                case GOTOBLOCK:
                    for(int j = 0; j < 1; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;

                    break;
                case CHOPBLOCK:
                case MINEBLOCK:
                case HARVESTBLOCK:
                case DUMPALLINCHEST:
                case PICKALLFROMCHEST:
                case GETBLOCKID:
                    for(int j = 0; j < 2; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--; //Every day we get closer to Yandere Dev. Truly the epitome of coding prowess. If else if if else else...

                case GETVECTORFLOAT:
                case SETVECTORFLOAT:
                    for(int j = 0; j < 4; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;
            }
        }
    }
    void RemoveNullPointers_Function(int pointer) {
        for(int i = 0; i < golem.instructions.length;i++)
        {
            switch (golem.instructions[i].ID) {case ADD:
                case SUBTRACT:
                case MULTIPLY:
                case DIVIDE:
                case MODULO:
                case LESS:
                case LESSOREQUAL:
                case EQUALS:
                case DISTANCE:
                case NOTEQUAL: {
                    for (int j = 3; j < 4; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;
                }
                break;
                case ROUND:
                {
                    for(int j = 2; j < 3; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;
                }
                break;
                case IFELSE:
                    for(int j = 1; j < 3; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;

                    break;
                case GETPOSITION:
                case GOTOBLOCK:
                    for(int j = 1; j < 2; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;
                    break;
                case CHOPBLOCK:
                case MINEBLOCK:
                case HARVESTBLOCK:
                case DUMPALLINCHEST:
                case PICKALLFROMCHEST:
                case GETBLOCKID:
                    for(int j = 2; j < 3; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;
                case GETVECTORFLOAT:
                case SETVECTORFLOAT:
                    for(int j = 4; j < 5; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;
                case DROPALLITEMS:
                    for(int j = 0; j < 1; j++)
                        if (golem.instructions[i].Pointers[j] == pointer)
                            golem.instructions[i].Pointers[j] = -1;
                        else if (golem.instructions[i].Pointers[j] > pointer)
                            golem.instructions[i].Pointers[j]--;
            }
        }
    }

    void BakeElements(bakeType BakeType, int index, LumenInstruction.Instruction_Codec Value, LumenGolemComponent.Type_Codec Var_Value) {
        switch(BakeType) {
            case ADD_VARIABLE: {

                int number = golem.data.length + 1;

                Object[] data = new Object[number];
                LumenGolemComponent.Type_Codec[] data_types = new LumenGolemComponent.Type_Codec[number];
                String[] data_names = new String[number];

                System.arraycopy(golem.data, 0, data, 0, golem.data.length);
                System.arraycopy(golem.data_types, 0, data_types, 0, golem.data.length);
                System.arraycopy(golem.data_names, 0, data_names, 0, golem.data.length);

                data[number - 1] = false;
                data_types[number - 1] = LumenGolemComponent.Type_Codec.BOOL;
                data_names[number - 1] = "VARIABLE" + (golem.data.length + 1);

                golem.data = data;
                golem.data_types = data_types;
                golem.data_names = data_names;

                return;
            }
            case REMOVE_VARIABLE:{


                int number = golem.data.length - 1;

                Object[] data = new Object[number];
                LumenGolemComponent.Type_Codec[] data_types = new LumenGolemComponent.Type_Codec[number];
                String[] data_names = new String[number];

                for(int i = 0; i < data.length;i++)
                {
                    if(i < index) {
                        data[i] = golem.data[i];
                        data_types[i] = golem.data_types[i];
                        data_names[i] = golem.data_names[i];
                    }
                    else {
                        data[i] = golem.data[i + 1];
                        data_types[i] = golem.data_types[i + 1];
                        data_names[i] = golem.data_names[i + 1];
                    }
                }

                golem.data = data;
                golem.data_types = data_types;
                golem.data_names = data_names;

                RemoveNullPointers_Variable(index);

                return;
            }
            case ADD_FUNCTION: {
                int number = golem.instructions.length + 1;
                LumenInstruction[] instructions = new LumenInstruction[number];
                System.arraycopy(golem.instructions, 0, instructions, 0, golem.instructions.length);
                instructions[number - 1] = new LumenInstruction("INSTRUCTION_" + (golem.instructions.length + 1), LumenInstruction.Instruction_Codec.ADD);
                instructions[number - 1].Pointers = new int[]
                        {-1, -1, -1, -1};
                golem.instructions = instructions;
                return;
            }
            case REMOVE_FUNCTION: {
                int number = golem.instructions.length - 1;
                LumenInstruction[] instructions = new LumenInstruction[number];
                for(int i = 0; i < instructions.length;i++)
                {
                    if(i < index)
                        instructions[i] = golem.instructions[i];
                    else
                        instructions[i] = golem.instructions[i + 1];
                }
                golem.instructions = instructions;
                RemoveNullPointers_Function(index);
                return;
            }
            case MODIFY_VARIABLE: {
                golem.data_types[index] = Var_Value;

                switch(Var_Value) {
                    case STRING -> golem.data[index] = "";
                    case BOOL -> golem.data[index] = false;
                    case FLOAT -> golem.data[index] = 0f;
                    case POS -> golem.data[index] = new Vector3d();
                    //block
                }

                return;
            }
            case MODIFY_FUNCTION: {
                golem.instructions[index] = new LumenInstruction(golem.instructions[index].Name, Value);
                golem.instructions[index].Pointers = new int[]
                        {-1, -1, -1, -1, -1, -1}; //it is inefficient. yes. no, i do not care.
                return;
            }
        }
    }

    private void buildElements(@Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {

        uiCommandBuilder.clear("#ListVariables");
        uiCommandBuilder.clear("#ListInstructions");
        BuildAll(this.golem, uiCommandBuilder, uiEventBuilder);
    }

    //due to 4d time limit, screw this. just inefficiently rebuild everything. alr running outta time. Enjoy your lag.

    ArrayList<String> FuncStrings; //init in buildelements
    ArrayList<String> VarStrings;

    void VariableAddTypeDropdown(String varName, int funcIndex, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {

        uiCommandBuilder.append("#ListVariables[" + funcIndex + "] #ListFields", "Pages/LumenGolemDropDown.ui");

        ObjectArrayList<DropdownEntryInfo> items = new ObjectArrayList();

        LumenGolemComponent.Type_Codec[] types = LumenGolemComponent.Type_Codec.values();
        for (int i = 0; i < types.length; i++)
            items.add(new DropdownEntryInfo(LocalizableString.fromString(types[i].name()), types[i].name()));

        uiCommandBuilder.set("#ListVariables[" + funcIndex + "] #ListFields[" + 1 + "] #Dropdown.Entries", items);
        uiCommandBuilder.set("#ListVariables[" + funcIndex + "] #ListFields[" + 1 + "] #Label.Text", varName);

        uiCommandBuilder.set("#ListVariables[" + funcIndex + "] #ListFields[" + 1 + "] #Dropdown.Value",
                golem.data_types[funcIndex].name());

    }


    void VariableAddTextField(String varName, int funcIndex, int varIndex, String parsedInput, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {

        uiCommandBuilder.append("#ListVariables[" + funcIndex + "] #ListFields", "Pages/LumenGolemTextFieldVariable.ui");
        uiCommandBuilder.set("#ListVariables[" + funcIndex + "] #ListFields[" + varIndex + "] #Label.Text", varName);

        uiCommandBuilder.set("#ListVariables[" + funcIndex + "] #ListFields[" + varIndex + "] #Text.Value",
                parsedInput);

    }



    void VariableAddBoolDropdown(String varName, int funcIndex, int varIndex, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {

        uiCommandBuilder.append("#ListVariables[" + funcIndex + "] #ListFields", "Pages/LumenGolemDropDown.ui");

        ObjectArrayList<DropdownEntryInfo> items = new ObjectArrayList();

        items.add(new DropdownEntryInfo(LocalizableString.fromString("FALSE"), "FALSE"));
        items.add(new DropdownEntryInfo(LocalizableString.fromString("TRUE"), "TRUE"));

        uiCommandBuilder.set("#ListVariables[" + funcIndex + "] #ListFields[" + varIndex + "] #Dropdown.Entries", items);
        uiCommandBuilder.set("#ListVariables[" + funcIndex + "] #ListFields[" + varIndex + "] #Label.Text", varName);

        if(golem.data[funcIndex] != null && golem.data[funcIndex].equals(true))
            uiCommandBuilder.set("#ListVariables[" + funcIndex + "] #ListFields[" + varIndex + "] #Dropdown.Value", "TRUE");
        else
            uiCommandBuilder.set("#ListVariables[" + funcIndex + "] #ListFields[" + varIndex + "] #Dropdown.Value", "FALSE");

    }

    void FunctionAddTypeDropdown(String varName, int funcIndex, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {

        uiCommandBuilder.append("#ListInstructions[" + funcIndex + "] #ListFields", "Pages/LumenGolemDropDown.ui");

        ObjectArrayList<DropdownEntryInfo> items = new ObjectArrayList();

        LumenInstruction.Instruction_Codec[] types = LumenInstruction.Instruction_Codec.values();
        for (int i = 0; i < types.length; i++)
            items.add(new DropdownEntryInfo(LocalizableString.fromString(types[i].name()), types[i].name()));

        uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + 1 + "] #Dropdown.Entries", items);
        uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + 1 + "] #Label.Text", varName);

        uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + 1 + "] #Dropdown.Value",
                golem.instructions[funcIndex].ID.name());

    }

    void FunctionAddFunctionDropdown(String varName, int funcIndex, int varIndex, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {

        uiCommandBuilder.append("#ListInstructions[" + funcIndex + "] #ListFields", "Pages/LumenGolemDropDown.ui");

        ObjectArrayList<DropdownEntryInfo> items = new ObjectArrayList();
        items.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.golemPage.noFunctions"), "-1"));

        FuncStrings.forEach((o) -> {
            items.add(new DropdownEntryInfo(LocalizableString.fromString(o), o));
        });
        uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + (varIndex + 2) + "] #Dropdown.Entries", items);
        uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + (varIndex + 2) + "] #Label.Text", varName);

        if(golem.instructions[funcIndex].Pointers[varIndex] != -1)
            uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + (varIndex + 2) + "] #Dropdown.Value",
                    FuncStrings.get(golem.instructions[funcIndex].Pointers[varIndex]));
        else
            uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + (varIndex + 2) + "] #Dropdown.Value",
                    "-1");

    }

    void FunctionAddVarDropdown(String varName, int funcIndex, int varIndex, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {

        uiCommandBuilder.append("#ListInstructions[" + funcIndex + "] #ListFields", "Pages/LumenGolemDropDown.ui");


        ObjectArrayList<DropdownEntryInfo> items = new ObjectArrayList();
        if(VarStrings.size() == 0)
            items.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.golemPage.noVariables"), "-1"));

        VarStrings.forEach((o) -> {
            items.add(new DropdownEntryInfo(LocalizableString.fromString(o), o));
        });
        uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + (varIndex + 2) + "] #Dropdown.Entries", items);
        uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + (varIndex + 2) + "] #Label.Text", varName);
        if(golem.instructions[funcIndex].Pointers[varIndex] != -1)
            uiCommandBuilder.set("#ListInstructions[" + funcIndex + "] #ListFields[" + (varIndex + 2) + "] #Dropdown.Value",
                    VarStrings.get(golem.instructions[funcIndex].Pointers[varIndex]));
    }

    void BuildAll(LumenGolemComponent component, @Nonnull UICommandBuilder uiCommandBuilder, @Nonnull UIEventBuilder uiEventBuilder) {
        FuncStrings = new ArrayList<>();
        for(int i = 0; i < component.instructions.length;i++) {
            FuncStrings.add(component.instructions[i].Name);
        }
        VarStrings = new ArrayList<>();
        VarStrings.addAll(Arrays.asList(component.data_names));

        for(int i = 0; i < component.instructions.length;i++) {
            uiCommandBuilder.append("#ListInstructions", "Pages/LumenGolemScriptElement.ui");
            uiCommandBuilder.set("#ListInstructions[" + i + "] #Name.Text", "Instruction");

            uiCommandBuilder.append("#ListInstructions[" + i + "] #ListFields", "Pages/LumenGolemTextField.ui");

            uiCommandBuilder.set("#ListInstructions[" + i + "] #ListFields[" + (0) + "] #Text.Value", component.instructions[i].Name);

            uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ListInstructions[" + i + "] #DeleteButton",
                    EventData.of("Button", "DeleteFunction").append("FuncIndex", Integer.toString(i)), false);


            FunctionAddTypeDropdown("Type", i, uiCommandBuilder, uiEventBuilder);
            uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ListInstructions[" + i + "] #ListFields[" + (1) + "] #Dropdown",
                    EventData.of("Button", "ChangeFunctionType")
                            .append("FuncIndex", Integer.toString(i))
                            .append("@Type", "#ListInstructions[" + i + "] #ListFields[" + (1) + "] #Dropdown.Value"), false);


            switch(component.instructions[i].ID) {
                case ADD:
                case SUBTRACT:
                case MULTIPLY:
                case DIVIDE:
                case MODULO:
                case LESS:
                case LESSOREQUAL:
                case EQUALS:
                case DISTANCE:
                case NOTEQUAL:
                {
                    FunctionAddVarDropdown("Input 1", i, 0, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Input 2", i, 1, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Output", i, 2, uiCommandBuilder, uiEventBuilder);
                    FunctionAddFunctionDropdown("Next Function", i, 3, uiCommandBuilder, uiEventBuilder);

                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListInstructions[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveFunction"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(3))
                                    .append("@Name", "#ListInstructions[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListInstructions[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value")
                                    .append("@V2", "#ListInstructions[" + i + "] #ListFields[" + 3 + "] #Dropdown.Value")
                                    .append("@V3", "#ListInstructions[" + i + "] #ListFields[" + 4 + "] #Dropdown.Value")
                                    .append("@V4", "#ListInstructions[" + i + "] #ListFields[" + 5 + "] #Dropdown.Value"));
                }
                break;
                case ROUND:
                {
                    FunctionAddVarDropdown("Variable", i, 0, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Output", i, 1, uiCommandBuilder, uiEventBuilder);
                    FunctionAddFunctionDropdown("Next Function", i, 2, uiCommandBuilder, uiEventBuilder);

                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListInstructions[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveFunction"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(2))
                                    .append("@Name", "#ListInstructions[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListInstructions[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value")
                                    .append("@V2", "#ListInstructions[" + i + "] #ListFields[" + 3 + "] #Dropdown.Value")
                                    .append("@V3", "#ListInstructions[" + i + "] #ListFields[" + 4 + "] #Dropdown.Value"));
                }
                break;
                case IFELSE:
                {
                    FunctionAddVarDropdown("Condition", i, 0, uiCommandBuilder, uiEventBuilder);
                    FunctionAddFunctionDropdown("If True", i, 1, uiCommandBuilder, uiEventBuilder);
                    FunctionAddFunctionDropdown("If False", i, 2, uiCommandBuilder, uiEventBuilder);

                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListInstructions[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveFunction"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(1))
                                    .append("@Name", "#ListInstructions[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListInstructions[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value")
                                    .append("@V2", "#ListInstructions[" + i + "] #ListFields[" + 3 + "] #Dropdown.Value")
                                    .append("@V3", "#ListInstructions[" + i + "] #ListFields[" + 4 + "] #Dropdown.Value"));
                }
                break;
                case GOTOBLOCK:
                {
                    FunctionAddVarDropdown("Block Position", i, 0, uiCommandBuilder, uiEventBuilder);
                    FunctionAddFunctionDropdown("Next Function", i, 1, uiCommandBuilder, uiEventBuilder);

                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListInstructions[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveFunction"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(1))
                                    .append("@Name", "#ListInstructions[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListInstructions[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value")
                                    .append("@V2", "#ListInstructions[" + i + "] #ListFields[" + 3 + "] #Dropdown.Value"));
                }
                break;
                case GETPOSITION:
                {
                    FunctionAddVarDropdown("Output Position", i, 0, uiCommandBuilder, uiEventBuilder);
                    FunctionAddFunctionDropdown("Next Function", i, 1, uiCommandBuilder, uiEventBuilder);

                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListInstructions[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveFunction"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(1))
                                    .append("@Name", "#ListInstructions[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListInstructions[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value")
                                    .append("@V2", "#ListInstructions[" + i + "] #ListFields[" + 3 + "] #Dropdown.Value"));
                }
                break;
                case CHOPBLOCK:
                case MINEBLOCK:
                case HARVESTBLOCK:
                case DUMPALLINCHEST:
                case PICKALLFROMCHEST:
                case GETBLOCKID:

                {
                    FunctionAddVarDropdown("Block Position", i, 0, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Output Completed", i, 1, uiCommandBuilder, uiEventBuilder);
                    FunctionAddFunctionDropdown("Next Function", i, 2, uiCommandBuilder, uiEventBuilder);

                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListInstructions[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveFunction"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(2))
                                    .append("@Name", "#ListInstructions[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListInstructions[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value")
                                    .append("@V2", "#ListInstructions[" + i + "] #ListFields[" + 3 + "] #Dropdown.Value")
                                    .append("@V3", "#ListInstructions[" + i + "] #ListFields[" + 4 + "] #Dropdown.Value"));
                }
                break;

                case DROPALLITEMS:
                {
                    FunctionAddFunctionDropdown("Next Function", i, 0, uiCommandBuilder, uiEventBuilder);

                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListInstructions[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveFunction"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(0))
                                    .append("@Name", "#ListInstructions[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListInstructions[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value"));
                }
                break;
                case GETVECTORFLOAT:
                {
                    FunctionAddVarDropdown("Vector", i, 0, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Output X", i, 1, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Output Y", i, 2, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Output Z", i, 3, uiCommandBuilder, uiEventBuilder);
                    FunctionAddFunctionDropdown("Next Function", i, 4, uiCommandBuilder, uiEventBuilder);

                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListInstructions[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveFunction"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(4))
                                    .append("@Name", "#ListInstructions[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListInstructions[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value")
                                    .append("@V2", "#ListInstructions[" + i + "] #ListFields[" + 3 + "] #Dropdown.Value")
                                    .append("@V3", "#ListInstructions[" + i + "] #ListFields[" + 4 + "] #Dropdown.Value")
                                    .append("@V4", "#ListInstructions[" + i + "] #ListFields[" + 5 + "] #Dropdown.Value")
                                    .append("@V5", "#ListInstructions[" + i + "] #ListFields[" + 6 + "] #Dropdown.Value"));
                }
                break;
                case SETVECTORFLOAT:
                {
                    FunctionAddVarDropdown("Vector", i, 0, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Input X", i, 1, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Input Y", i, 2, uiCommandBuilder, uiEventBuilder);
                    FunctionAddVarDropdown("Input Z", i, 3, uiCommandBuilder, uiEventBuilder);
                    FunctionAddFunctionDropdown("Next Function", i, 4, uiCommandBuilder, uiEventBuilder);

                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListInstructions[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveFunction"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(4))
                                    .append("@Name", "#ListInstructions[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListInstructions[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value")
                                    .append("@V2", "#ListInstructions[" + i + "] #ListFields[" + 3 + "] #Dropdown.Value")
                                    .append("@V3", "#ListInstructions[" + i + "] #ListFields[" + 4 + "] #Dropdown.Value")
                                    .append("@V4", "#ListInstructions[" + i + "] #ListFields[" + 5 + "] #Dropdown.Value")
                                    .append("@V5", "#ListInstructions[" + i + "] #ListFields[" + 6 + "] #Dropdown.Value"));
                }
                break;
            }
        }

        for(int i = 0; i < component.data_names.length;i++) {

            uiCommandBuilder.append("#ListVariables", "Pages/LumenGolemScriptElement.ui");
            uiCommandBuilder.set("#ListVariables[" + i + "] #Name.Text", "Variable");

            uiCommandBuilder.append("#ListVariables[" + i + "] #ListFields", "Pages/LumenGolemTextField.ui");

            uiCommandBuilder.set("#ListVariables[" + i + "] #ListFields[" + (0) + "] #Text.Value", component.data_names[i]);

            uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#ListVariables[" + i + "] #DeleteButton",
                    EventData.of("Button", "DeleteVariable").append("FuncIndex", Integer.toString(i)), false);

            VariableAddTypeDropdown("Type", i, uiCommandBuilder, uiEventBuilder);
            uiEventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#ListVariables[" + i + "] #ListFields[" + (1) + "] #Dropdown",
                    EventData.of("Button", "ChangeVariableType")
                            .append("FuncIndex", Integer.toString(i))
                            .append("@Type", "#ListVariables[" + i + "] #ListFields[" + (1) + "] #Dropdown.Value"), false);


            switch(component.data_types[i]) {
                case BOOL -> {
                    VariableAddBoolDropdown("Boolean", i, 2, uiCommandBuilder, uiEventBuilder);
                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListVariables[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveVariable"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(2))
                                    .append("@Type", "#ListVariables[" + i + "] #ListFields[" + (1) + "] #Dropdown.Value")
                                    .append("@Name", "#ListVariables[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListVariables[" + i + "] #ListFields[" + 2 + "] #Dropdown.Value"));
                }
                case FLOAT -> {
                    VariableAddTextField("Number", i, 2, Double.toString((Float) golem.data[i]), uiCommandBuilder, uiEventBuilder);
                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListVariables[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveVariable"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(2))
                                    .append("@Type", "#ListVariables[" + i + "] #ListFields[" + (1) + "] #Dropdown.Value")
                                    .append("@Name", "#ListVariables[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListVariables[" + i + "] #ListFields[" + 2 + "] #Text.Value"));
                }
                case STRING -> {
                    VariableAddTextField("String", i, 2, (String) golem.data[i], uiCommandBuilder, uiEventBuilder);
                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListVariables[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveVariable"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(2))
                                    .append("@Type", "#ListVariables[" + i + "] #ListFields[" + (1) + "] #Dropdown.Value")
                                    .append("@Name", "#ListVariables[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListVariables[" + i + "] #ListFields[" + 2 + "] #Text.Value"));
                }
                case POS -> {
                    VariableAddTextField("X", i,2, Double.toString(((Vector3d)golem.data[i]).x), uiCommandBuilder, uiEventBuilder);
                    VariableAddTextField("Y", i,3, Double.toString(((Vector3d)golem.data[i]).y), uiCommandBuilder, uiEventBuilder);
                    VariableAddTextField("Z", i,4, Double.toString(((Vector3d)golem.data[i]).z), uiCommandBuilder, uiEventBuilder);
                    uiEventBuilder.addEventBinding(CustomUIEventBindingType.Activating,
                            "#ListVariables[" + i + "] #SaveButton",
                            (EventData.of("Button", "SaveVariable"))
                                    .append("FuncIndex", Integer.toString(i))
                                    .append("RIndex", Integer.toString(2))
                                    .append("@Type", "#ListVariables[" + i + "] #ListFields[" + (1) + "] #Dropdown.Value")
                                    .append("@Name", "#ListVariables[" + i + "] #ListFields[" + 0 + "] #Text.Value")
                                    .append("@V1", "#ListVariables[" + i + "] #ListFields[" + 2 + "] #Text.Value")
                                    .append("@V2", "#ListVariables[" + i + "] #ListFields[" + 3 + "] #Text.Value")
                                    .append("@V3", "#ListVariables[" + i + "] #ListFields[" + 4 + "] #Text.Value"));
                }
            }
        }
    }

    //data

    public static class Data {
        static final String KEY_BUTTON = "Button";
        static final String KEY_REMOVE_BUTTON_ACTION = "RemoveButtonAction";
        static final String KEY_DROPDOWN = "Dropdown";


        public static final BuilderCodec<Data> CODEC = BuilderCodec.builder(Data.class, Data::new)
                .append(new KeyedCodec<>(KEY_BUTTON, Codec.STRING),
                        (data, value) -> data.Button = value,
                        data -> data.Button).add()
                .append(new KeyedCodec<>(KEY_DROPDOWN, Codec.STRING),
                        (data, value) -> data.Dropdown = value,
                        data -> data.Dropdown).add()
                .append(new KeyedCodec<>("@Name", Codec.STRING),
                        (data, value) -> data.Name = value,
                        data -> data.Name).add()
                .append(new KeyedCodec<>("@DropdownVal", Codec.STRING),
                        (data, value) -> data.DropdownVal = value,
                        data -> data.DropdownVal).add()
                .append(new KeyedCodec<>("@Type", Codec.STRING),
                        (data, value) -> data.Type = value,
                        data -> data.Type).add()
                .append(new KeyedCodec<>("FuncIndex", Codec.STRING),
                        (data, value) -> data.FuncIndex = value,
                        data -> data.FuncIndex).add()
                .append(new KeyedCodec<>("RIndex", Codec.STRING),
                        (data, value) -> data.RIndex = value,
                        data -> data.RIndex).add()
                .append(new KeyedCodec<>("@V1", Codec.STRING),
                        (data, value) -> data.Variables[0] = value,
                        data -> data.Variables[0]).add()
                .append(new KeyedCodec<>("@V2", Codec.STRING),
                        (data, value) -> data.Variables[1] = value,
                        data -> data.Variables[1]).add()
                .append(new KeyedCodec<>("@V3", Codec.STRING),
                        (data, value) -> data.Variables[2] = value,
                        data -> data.Variables[2]).add()
                .append(new KeyedCodec<>("@V4", Codec.STRING),
                        (data, value) -> data.Variables[3] = value,
                        data -> data.Variables[3]).add()
                .build();


        private String Button;
        private String Dropdown;
        private String DropdownVal;

        private String Name;

        private String Type;
        private String FuncIndex;
        private String RIndex;
        private String[] Variables = new String[10];
    }
}
