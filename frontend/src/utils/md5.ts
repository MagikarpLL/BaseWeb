/**
 * Simple MD5 implementation for Gravatar email hashing
 * Based on RFC 1321 algorithm
 */

function md5cycle(x: number[], k: number[]): number[] {
  let a = x[0], b = x[1], c = x[2], d = x[3]

  a = ff(a, b, c, d, k[0], 7, -680876936)
  d = ff(d, a, b, c, k[1], 12, -389564586)
  c = ff(c, d, a, b, k[2], 17, 606105819)
  b = ff(b, c, d, a, k[3], 22, -1044527070)
  a = ff(a, b, c, d, k[4], 7, -176418897)
  d = ff(d, a, b, c, k[5], 12, 1200080426)
  c = ff(c, d, a, b, k[6], 17, -1473231341)
  b = ff(b, c, d, a, k[7], 22, -45705983)
  a = ff(a, b, c, d, k[8], 7, 1770035416)
  d = ff(d, a, b, c, k[9], 12, -1958414417)
  c = ff(c, d, a, b, k[10], 17, -42063)
  b = ff(b, c, d, a, k[11], 22, -1990404162)
  a = ff(a, b, c, d, k[12], 7, 1804603682)
  d = ff(d, a, b, c, k[13], 12, -40341101)
  c = ff(c, d, a, b, k[14], 17, -1502002290)
  b = ff(b, c, d, a, k[15], 22, 1236535329)

  a = gg(a, b, c, d, k[1], 5, -165796510)
  d = gg(d, a, b, c, k[6], 9, -1069501632)
  c = gg(c, d, a, b, k[11], 14, 643717713)
  b = gg(b, c, d, a, k[0], 20, -373897302)
  a = gg(a, b, c, d, k[5], 5, -701558691)
  d = gg(d, a, b, c, k[10], 9, 38016083)
  c = gg(c, d, a, b, k[15], 14, -660478335)
  b = gg(b, c, d, a, k[4], 20, -405537848)
  a = gg(a, b, c, d, k[9], 5, 568446438)
  d = gg(d, a, b, c, k[14], 9, -1019803690)
  c = gg(c, d, a, b, k[3], 14, -187363961)
  b = gg(b, c, d, a, k[8], 20, 1163531501)
  a = gg(a, b, c, d, k[13], 5, -1444681467)
  d = gg(d, a, b, c, k[2], 9, -51403784)
  c = gg(c, d, a, b, k[7], 14, 1735328473)
  b = gg(b, c, d, a, k[12], 20, -1926607734)

  a = hh(a, b, c, d, k[5], 4, -378558)
  d = hh(d, a, b, c, k[8], 11, -2022574463)
  c = hh(c, d, a, b, k[11], 16, 1839030562)
  b = hh(b, c, d, a, k[14], 23, -35309556)
  a = hh(a, b, c, d, k[1], 4, -1530992060)
  d = hh(d, a, b, c, k[4], 11, 1272893353)
  c = hh(c, d, a, b, k[7], 16, -155497632)
  b = hh(b, c, d, a, k[10], 23, -1094730640)
  a = hh(a, b, c, d, k[13], 4, 681279174)
  d = hh(d, a, b, c, k[0], 11, -358537222)
  c = hh(c, d, a, b, k[3], 16, -722521979)
  b = hh(b, c, d, a, k[6], 23, 76029189)
  a = hh(a, b, c, d, k[9], 4, -640364487)
  d = hh(d, a, b, c, k[12], 11, -421815835)
  c = hh(c, d, a, b, k[15], 16, 530742520)
  b = hh(b, c, d, a, k[2], 23, -995338651)

  a = ii(a, b, c, d, k[0], 6, -198630844)
  d = ii(d, a, b, c, k[7], 10, 1126891415)
  c = ii(c, d, a, b, k[14], 15, -1416354905)
  b = ii(b, c, d, a, k[5], 21, -57434055)
  a = ii(a, b, c, d, k[12], 6, 1700485571)
  d = ii(d, a, b, c, k[3], 10, -1894986606)
  c = ii(c, d, a, b, k[10], 15, -1051523)
  b = ii(b, c, d, a, k[1], 21, -2054922799)
  a = ii(a, b, c, d, k[8], 6, 1873313359)
  d = ii(d, a, b, c, k[15], 10, -30611744)
  c = ii(c, d, a, b, k[6], 15, -1560198380)
  b = ii(b, c, d, a, k[13], 21, 1309151649)
  a = ii(a, b, c, d, k[4], 6, -145523070)
  d = ii(d, a, b, c, k[11], 10, -1120210379)
  c = ii(c, d, a, b, k[2], 15, 718787259)
  b = ii(b, c, d, a, k[9], 21, -343485551)

  x[0] = add32(a, x[0])
  x[1] = add32(b, x[1])
  x[2] = add32(c, x[2])
  x[3] = add32(d, x[3])

  return x
}

function ff(a: number, b: number, c: number, d: number, x: number, s: number, ac: number): number {
  a = add32(bitrol(add32(a, add32(bitand(b, c), add32(bitand(~b, d), x))), s), ac)
  return a
}

function gg(a: number, b: number, c: number, d: number, x: number, s: number, ac: number): number {
  a = add32(bitrol(add32(a, add32(bitand(b, d), add32(bitand(c, ~d), x))), s), ac)
  return a
}

function hh(a: number, b: number, c: number, d: number, x: number, s: number, ac: number): number {
  a = add32(bitrol(add32(a, add32(xor(b, c), add32(bitand(d, ~c), x))), s), ac)
  return a
}

function ii(a: number, b: number, c: number, d: number, x: number, s: number, ac: number): number {
  a = add32(bitrol(add32(a, add32(xor(c, d), add32(bitand(b, ~d), x))), s), ac)
  return a
}

function bitand(a: number, b: number): number {
  return a & b
}

function bitrol(a: number, num: number): number {
  return (a << num) | (a >>> (32 - num))
}

function add32(a: number, b: number): number {
  return (a + b) & 0xFFFFFFFF
}

function xor(a: number, b: number): number {
  return a ^ b
}

function md5blk(s: string): number[] {
  const md5blks: number[] = []
  for (let i = 0; i < 64; i += 4) {
    md5blks[i >> 2] = 
      (s.charCodeAt(i) & 0xff) |
      ((s.charCodeAt(i + 1) & 0xff) << 8) |
      ((s.charCodeAt(i + 2) & 0xff) << 16) |
      ((s.charCodeAt(i + 3) & 0xff) << 24)
  }
  return md5blks
}

const hexChr = '0123456789abcdef'.split('')

function rhex(n: number): string {
  let s = ''
  for (let j = 0; j < 4; j++) {
    s += hexChr[(n >> (j * 8 + 4)) & 0x0f] + hexChr[(n >> (j * 8)) & 0x0f]
  }
  return s
}

function md5(s: string): string {
  let state = [1732584193, -271733879, -1732584194, 271733878]
  let i: number
  
  s = utf8Encode(s)
  
  const nblk = Math.floor((s.length + 8) / 64) + 1
  const blks: number[][] = new Array(nblk)
  
  for (i = 0; i < nblk; i++) {
    blks[i] = md5blk(s.slice(i * 64, i * 64 + 64))
  }
  
  for (i = 0; i < nblk; i++) {
    state = md5cycle(state, blks[i])
  }
  
  return rhex(state[0]) + rhex(state[1]) + rhex(state[2]) + rhex(state[3])
}

function utf8Encode(s: string): string {
  return s.replace(/[\u0080-\uffff]/g, (c) => {
    return 'xxx' + escape(c).replace('%', '')
  })
}

export function getGravatarUrl(email: string, size: number = 48): string {
  if (!email) return ''
  const hash = md5(email.trim().toLowerCase())
  return `https://www.gravatar.com/avatar/${hash}?s=${size}&d=identicon&r=g`
}

export function md5hash(str: string): string {
  return md5(str)
}