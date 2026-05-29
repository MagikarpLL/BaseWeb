import requests
import docker

# Register a new user to get a valid BCrypt hash from the server
r = requests.post('http://localhost:8080/api/auth/register',
    json={'username': 'hashtest', 'password': 'admin123'}
)
data = r.json()
hash_value = data['data']['password']
print(f'Got hash from server: {hash_value}')

# Use docker python SDK to update admin password
client = docker.from_env()
result = client.containers.get('personal_website_db').exec_run(
    ['psql', '-U', 'postgres', '-d', 'personal_website_dev', '-c',
     f"UPDATE users SET password = '{hash_value}' WHERE username = 'admin'"]
)
print('Update result:', result.output.decode())

# Verify
r2 = client.containers.get('personal_website_db').exec_run(
    ['psql', '-U', 'postgres', '-d', 'personal_website_dev', '-c',
     "SELECT username, password FROM users WHERE username = 'admin'"]
)
print('Verification:', r2.output.decode())