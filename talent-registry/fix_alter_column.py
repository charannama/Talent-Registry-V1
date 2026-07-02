import os
import re

directory = 'src/main/resources/db/migration'

for filename in os.listdir(directory):
    if not filename.endswith('.sql'):
        continue
    filepath = os.path.join(directory, filename)
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    changed = False

    # 1. Remove USING clauses from ALTER COLUMN
    # e.g. ALTER COLUMN created_by TYPE VARCHAR(255) USING created_by::text
    new_content = re.sub(r'(ALTER\s+COLUMN\s+\w+\s+TYPE\s+[^,;]+)\s+USING\s+[^,;]+', r'\1', content, flags=re.IGNORECASE)
    if new_content != content:
        changed = True
        content = new_content

    # 2. Split comma-separated ALTER COLUMN
    # It's similar to ADD COLUMN. Let's just find "ALTER TABLE x ALTER COLUMN y, ALTER COLUMN z;"
    # and split them manually.
    
    parts = re.split(r'(ALTER\s+TABLE\s+[a-zA-Z0-9_]+)', content, flags=re.IGNORECASE)
    
    if len(parts) > 1:
        new_content = parts[0]
        for i in range(1, len(parts), 2):
            alter_table_stmt = parts[i]
            body = parts[i+1]
            
            semicolon_idx = body.find(';')
            if semicolon_idx == -1:
                semicolon_idx = len(body)
                
            stmt_body = body[:semicolon_idx]
            rest = body[semicolon_idx+1:]
            
            if re.search(r',\s*ALTER\s+COLUMN', stmt_body, re.IGNORECASE):
                sub_stmts = re.split(r',\s*(?=\bALTER\s+COLUMN\b)', stmt_body, flags=re.IGNORECASE)
                
                new_stmt = ""
                for idx, sub_stmt in enumerate(sub_stmts):
                    if idx > 0:
                        new_stmt += f";\n{alter_table_stmt} " + sub_stmt.strip()
                    else:
                        new_stmt += sub_stmt
                
                new_content += alter_table_stmt + new_stmt + ";" + rest
                changed = True
            else:
                if semicolon_idx != len(body):
                    new_content += alter_table_stmt + body[:semicolon_idx] + ";" + rest
                else:
                    new_content += alter_table_stmt + body
        content = new_content

    if changed:
        print(f"Fixed {filename}")
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
