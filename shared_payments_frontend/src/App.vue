<template>
  <div id="app">
    <nav class="navbar navbar-expand navbar-dark bg-dark">
      <router-link to="/" class="navbar-brand">Shared Payments</router-link>
      <div class="navbar-nav mr-auto">
        <li class="nav-item">
          <router-link to="/groups" class="nav-link">Groups</router-link>
        </li>
        <li class="nav-item">
          <router-link to="/add-group" class="nav-link">Add Group</router-link>
        </li>
      </div>
    </nav>
    <div class="container mt-3">
      <router-view />
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'App'
})
</script>
