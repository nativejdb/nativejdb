/*******************************************************************************
 * Copyright (c) 2017, 2018 Kichwa Coders Ltd and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation
 *******************************************************************************/
package gdb.mi.service.command.output;

import java.util.LinkedList;
import java.util.List;

/**
 * Example output is:
 *
 * <pre>
 *    (gdb) -symbol-info-variables
 *   ^done,symbols=
 *     {debug=
 *     [{filename="/project/gdb/testsuite/gdb.mi/mi-sym-info-1.c",
 *     fullname="/project/gdb/testsuite/gdb.mi/mi-sym-info-1.c",
 *     symbols=[{line="25",name="global_f1",type="float",
 *     		description="static float global_f1;"},
 *     		{line="24",name="global_i1",type="int",
 *     		description="static int global_i1;"}]},
 *     {filename="/project/gdb/testsuite/gdb.mi/mi-sym-info-2.c",
 *     fullname="/project/gdb/testsuite/gdb.mi/mi-sym-info-2.c",
 *     symbols=[{line="21",name="global_f2",type="int",
 *     		description="int global_f2;"},
 *     		{line="20",name="global_i2",type="int",
 *     		description="int global_i2;"},
 *     		{line="19",name="global_f1",type="float",
 *     		description="static float global_f1;"},
 *     		{line="18",name="global_i1",type="int",
 *     		description="static int global_i1;"}]}]}
 * </pre>
 */
public class MiSymbolInfoVariablesInfo extends MIInfo {

	private SymbolVariableInfo[] symbolVariableInfos;

	public MiSymbolInfoVariablesInfo(MIOutput record) {
		super(record);
		parse();
		if (symbolVariableInfos == null) {
			symbolVariableInfos = new SymbolVariableInfo[0];
		}
	}

	/**
	 * Returns array of symbol variables infos
	 *
	 * @return
	 */
	public SymbolVariableInfo[] getSymbolVariables() {
		return symbolVariableInfos;
	}

	private void parse() {
		if (isDone()) {
			MIOutput out = getMIOutput();
			MIResultRecord rr = out.getMIResultRecord();
			if (rr != null) {
				MIResult[] results = rr.getMIResults();
				for (int i = 0; i < results.length; i++) {
					String var = results[i].getVariable();
					if (var.equals("symbols")) { //$NON-NLS-1$
						results = ((MITuple)results[i].getMIValue()).getMIResults();
						for (int j = 0; j < results.length; j++) {
							var = results[j].getVariable();
							if (var.equals("debug")) {
								MIValue value = results[j].getMIValue();
								if (value instanceof MIList) {
									parseResults((MIList) value);
								}
							}
						}
					}
				}
			}
		}

	}

	private void parseResults(MIList list) {
		MIValue[] miValues = list.getMIValues();
		List<SymbolVariableInfo> infos = new LinkedList<>();
		if (miValues != null) {
			for (MIValue miValue : miValues) {
				if (miValue instanceof MITuple) {
					MITuple miTuple = (MITuple) miValue;
					SymbolVariableInfo info = new SymbolVariableInfo();
					info.parse(miTuple.getMIResults());
					infos.add(info);
				}
			}
		}
		symbolVariableInfos = infos.toArray(new SymbolVariableInfo[infos.size()]);
	}

	public static class SymbolVariableInfo {
		private String filename;
		private String fullname;
		private Symbols[] symbols;

		public void setFilename(String filename) {
			this.filename = filename;
		}

		public String getFilename() {
			return filename;
		}

		public void setFullName(String fullname) {
			this.fullname = fullname;
		}

		public String getFullName() {
			return fullname;
		}

		public void setSymbols(Symbols[] symbols) {
			this.symbols = symbols;
		}

		public Symbols[] getSymbols() {
			return symbols;
		}

		private void parse(MIResult[] results) {
			for (MIResult result : results) {
				String variable = result.getVariable();
				MIValue miVal = result.getMIValue();
				if (!(miVal instanceof MIConst) && !(miVal instanceof MIList)) {
					continue;
				}
				switch (variable) {
				case "filename": //$NON-NLS-1$
					filename = ((MIConst) miVal).getCString();
					break;
				case "fullname": //$NON-NLS-1$
					fullname = ((MIConst) miVal).getCString();
					break;
				case "symbols": //$NON-NLS-1$
					symbols = parseSymbols((MIList) miVal);
					break;
				}
			}
		}

		private Symbols[] parseSymbols(MIList list) {
			MIValue[] miValues = list.getMIValues();
			List<Symbols> infos = new LinkedList<>();
			if (miValues != null) {
				for (MIValue miValue : miValues) {
					if (miValue instanceof MITuple) {
						MITuple miTuple = (MITuple) miValue;
						Symbols info = new Symbols();
						info.parse(miTuple.getMIResults());
						infos.add(info);
					}
				}
			}
			symbols = infos.toArray(new Symbols[infos.size()]);
			return symbols;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((filename == null) ? 0 : filename.hashCode());
			result = prime * result + ((fullname == null) ? 0 : fullname.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SymbolVariableInfo other = (SymbolVariableInfo) obj;
			if (filename == null) {
				if (other.filename != null)
					return false;
			} else if (!filename.equals(other.filename))
				return false;
			if (fullname == null) {
				if (other.fullname != null)
					return false;
			} else if (!fullname.equals(other.fullname))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SymbolVariableInfo [filename=" + filename + ", fullname=" + fullname + ", symbols=" + symbols + "]";
		}

	}

	public static class Symbols {
		private String description;
		private String name;
		private String type;
		private int line;

		public void setDescription(String description) {
			this.description = description;
		}

		public String getDescription() {
			return description;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public int getLine() {
			return line;
		}

		public void setLine(int line) {
			this.line = line;
		}

		private void parse(MIResult[] results) {
			for (MIResult result : results) {
				String variable = result.getVariable();
				MIValue miVal = result.getMIValue();
				if (!(miVal instanceof MIConst)) {
					continue;
				}
				String value = ((MIConst) miVal).getCString();
				switch (variable) {
					case "name": //$NON-NLS-1$
						name = value;
						break;
					case "type": //$NON-NLS-1$
						type = value;
						break;
					case "line": //$NON-NLS-1$
						line = Integer.parseInt(value);
						break;
					case "description": //$NON-NLS-1$
						description = value;
						break;
				}
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result + ((description == null) ? 0 : description.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Symbols other = (Symbols) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			if (description == null) {
				if (other.description != null)
					return false;
			} else if (!description.equals(other.description))
				return false;
			 if (line != other.line)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "Symbols [name=" + name + ", type=" + type + ", description=" + description+ ", line=" + line + "]";
		}

	}
}
