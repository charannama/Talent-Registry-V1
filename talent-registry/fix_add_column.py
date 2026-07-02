import os
import re

directory = 'src/main/resources/db/migration'

for filename in os.listdir(directory):
    if filename.endswith('.sql'):
        filepath = os.path.join(directory, filename)
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # Regex to find ALTER TABLE <name> ... ADD COLUMN ...
        # This is a bit tricky to parse with regex.
        # Let's split by ALTER TABLE and process each block
        
        new_content = ""
        parts = re.split(r'(ALTER\s+TABLE\s+[a-zA-Z0-9_]+)', content, flags=re.IGNORECASE)
        
        if len(parts) == 1:
            continue
            
        new_content = parts[0]
        changed = False
        
        for i in range(1, len(parts), 2):
            alter_table_stmt = parts[i]
            body = parts[i+1]
            
            # Find the end of the statement (semicolon)
            semicolon_idx = body.find(';')
            if semicolon_idx == -1:
                # no semicolon? might be at the end of file
                semicolon_idx = len(body)
                
            stmt_body = body[:semicolon_idx]
            rest = body[semicolon_idx+1:]
            
            # Check if stmt_body has multiple ADD COLUMN separated by comma
            # E.g. " ADD COLUMN col1 INT, ADD COLUMN col2 INT"
            # We can split by ", ADD COLUMN" or ",\n ADD COLUMN" etc.
            
            if re.search(r',\s*ADD\s+COLUMN', stmt_body, re.IGNORECASE):
                # We have multiple ADD COLUMNs!
                # Split the stmt_body by comma only when it's followed by ADD COLUMN
                # But wait, it could be "ADD COLUMN col BOOLEAN DEFAULT FALSE, ADD COLUMN..."
                
                # Split by ",\s*(?=ADD\s+COLUMN)"
                sub_stmts = re.split(r',\s*(?=\bADD\s+COLUMN\b)', stmt_body, flags=re.IGNORECASE)
                
                new_stmt = ""
                for idx, sub_stmt in enumerate(sub_stmts):
                    if idx > 0:
                        new_stmt += f";\n{alter_table_stmt} " + sub_stmt.strip()
                    else:
                        new_stmt += sub_stmt
                
                new_content += alter_table_stmt + new_stmt + ";" + rest
                changed = True
            else:
                # restore original
                if semicolon_idx != len(body):
                    new_content += alter_table_stmt + body[:semicolon_idx] + ";" + rest
                else:
                    new_content += alter_table_stmt + body
                    
        if changed:
            print(f"Fixed {filename}")
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(new_content)
