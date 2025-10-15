const toggleButton = document.getElementById('toggle-btn')
const sidebar = document.getElementById('sidebar')

function toggleSidebar(){
  sidebar.classList.toggle('close')
  toggleButton.classList.toggle('rotate')

  closeAllSubMenus()
}

function toggleSubMenu(button){

  if(!button.nextElementSibling.classList.contains('show')){
    closeAllSubMenus()
  }

  button.nextElementSibling.classList.toggle('show')
  button.classList.toggle('rotate')

  if(sidebar.classList.contains('close')){
    sidebar.classList.toggle('close')
    toggleButton.classList.toggle('rotate')
  }
}

function closeAllSubMenus(){
  Array.from(sidebar.getElementsByClassName('show')).forEach(ul => {
    ul.classList.remove('show')
    ul.previousElementSibling.classList.remove('rotate')
  })
}


const searchForm = document.querySelector('.search-form')
if (searchForm) {
  const searchInput = searchForm.querySelector('input[type="search"]')

  
  searchForm.addEventListener('submit', (e) => e.preventDefault())

  searchForm.addEventListener('click', (e) => {
    
    if (e.target && e.target.tagName && e.target.tagName.toLowerCase() === 'input') return

    if (sidebar.classList.contains('close')) {
      
      toggleSidebar()
      
      setTimeout(() => searchInput && searchInput.focus(), 350)
    } else {
      
      searchInput && searchInput.focus()
    }
  })

  
}