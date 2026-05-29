import requests

# Register a new user to get a valid BCrypt hash
r = requests.post('http://localhost:8080/api/auth/register',
    json={'username': 'hashtest2', 'password': 'admin123'}
)
data = r.json()
hash_value = data['data']['password']
print(f'Got hash from server: {hash_value}')

# Write the hash to a file for later use
with open('F:\\Workspace\\VibeCoding\\personal-painpoints\\Base\\admin_hash.txt', 'w') as f:
    f.write(hash_value)

print(f'Hash saved to file: {hash_value}')