import type { Directive, DirectiveBinding } from 'vue'

const lazyload: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding) {
    if (!('IntersectionObserver' in window)) {
      // Fallback: just set src directly
      if (binding.value) (el as HTMLImageElement).src = binding.value
      return
    }

    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const img = entry.target as HTMLImageElement
            const src = binding.value
            if (src) {
              img.src = src
              img.classList.add('loaded')
            }
            observer.unobserve(img)
          }
        })
      },
      { rootMargin: '50px 0px', threshold: 0.01 }
    )
    observer.observe(el)
  }
}

export default lazyload
